package main.arbitrage.application.user.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.application.user.dto.UserProfileDto;
import main.arbitrage.auth.security.SecurityUtil;
import main.arbitrage.domain.exchangeRate.dto.ExchangeRateDto;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
import main.arbitrage.domain.oauthUser.service.OAuthUserService;
import main.arbitrage.domain.user.dto.UserEditNicknameDto;
import main.arbitrage.domain.userEnv.dto.UserEnvDto;
import main.arbitrage.domain.userEnv.entity.UserEnv;
import main.arbitrage.domain.userEnv.service.UserEnvService;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.dto.BinanceGetAccountResponseDto;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.UpbitGetAccountResponseDto;
import main.arbitrage.infrastructure.oauthValidator.google.GoogleApiClient;
import main.arbitrage.infrastructure.oauthValidator.kakao.KakaoApiClient;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;
import okhttp3.OkHttpClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.auth.jwt.JwtUtil;
import main.arbitrage.infrastructure.email.dto.EmailMessageDto;
import main.arbitrage.infrastructure.email.service.EmailMessageService;
import main.arbitrage.domain.user.dto.UserLoginDto;
import main.arbitrage.domain.user.dto.UserSignupDto;
import main.arbitrage.domain.user.dto.UserTokenDto;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.user.service.UserService;
import main.arbitrage.global.util.aes.AESCrypto;
import main.arbitrage.infrastructure.redis.service.RefreshTokenService;


@Service
@RequiredArgsConstructor
public class UserApplicationService {
    private final EmailMessageService emailMessageService;
    private final UserService userService;
    private final OAuthUserService oAuthUserService;
    private final ExchangeRateService exchangeRateService;
    private final RefreshTokenService refreshTokenService;
    private final UserEnvService userEnvService;
    private final AESCrypto aesCrypto;

    private final JwtUtil jwtUtil;

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    private final GoogleApiClient googleApiClient;
    private final KakaoApiClient kakaoApiClient;

    @Transactional
    public String sendEmail(EmailMessageDto emailMessageDto) throws Exception {
        String email = emailMessageDto.getTo();

        if (userService.existsByEmail(email)) {
            throw new DataIntegrityViolationException("This email is already in use: " + email);
        }

        String code = emailMessageService.sendMail(emailMessageDto, "email");

        return aesCrypto.encrypt(code.getBytes());
    }

    @Transactional
    public UserTokenDto signup(UserSignupDto req) {
        if (userService.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("This email is already in use");
        }

        String code = req.getCode();
        String encryptedCode = req.getEncryptedCode();
        String accessToken = req.getAccessToken();
        String provider = req.getProvider();
        String providerId = req.getProviderId();

        if (!code.isEmpty() && !encryptedCode.isEmpty()) {
            // OAuth login이 아닐시 code와 encryptedCode가 반드시 존재해야함
            if (!checkCode(code, encryptedCode)) throw new IllegalArgumentException("Invalid value");

            return userTokenResponseBuilder(userService.create(req));
        } else if (!accessToken.isEmpty() && !provider.isEmpty() && !providerId.isEmpty()) {
            if (!isCorrectOAuthToken(req)) throw new IllegalArgumentException("Invalid value");

            User user = userService.create(req);
            oAuthUserService.create(user, req);

            return userTokenResponseBuilder(user);
        } else {
            throw new IllegalArgumentException("Invalid value");
        }
    }

    @Transactional
    public UserTokenDto login(UserLoginDto req) {
        Optional<User> userOptional = userService.findByEmail(req.getEmail());

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid info");
        }

        User user = userOptional.get();

        if (!userService.matchPassword(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid info");
        }

        return userTokenResponseBuilder(user);
    }

    @Transactional
    public void registerUserEnv(UserEnvDto req) throws Exception {
        Long userId = SecurityUtil.getUserId();

        Optional<User> optionalUser = userService.findByEmail(SecurityUtil.getEmail());

        if (optionalUser.isEmpty()) throw new IllegalArgumentException("user is not found");

        // upbit accessKey와 secretKey를 지갑 잔액을 조회함으로써 올바른 키가 맞는지 증명
        UpbitPrivateRestService upbitPrivateRestService =
                new UpbitPrivateRestService(req.getUpbitAccessKey(), req.getUpbitSecretKey(), okHttpClient, objectMapper);

        upbitPrivateRestService.getAccount();

        // binance accessKey와 secretKey를 지갑 잔액을 조회함으로써 올바른 키가 맞는지 증명
        BinancePrivateRestService binancePrivateRestService =
                new BinancePrivateRestService(req.getBinanceAccessKey(), req.getBinanceSecretKey(), okHttpClient, objectMapper);

        binancePrivateRestService.getAccount();

        Optional<UserEnv> optionalUserEnv = userEnvService.findByUserId(userId);

        if (optionalUserEnv.isEmpty()) {
            userEnvService.create(UserEnvDto.toEntity(req, optionalUser.get(), aesCrypto));
        } else {
            UserEnv userEnv = optionalUserEnv.get();
            userEnv.updateEnv(req, aesCrypto);
        }
    }

    @Transactional
    public UserProfileDto getUserProfile() throws Exception {
        Long userId = SecurityUtil.getUserId();

        // userId에 해당하는 env가 있는지 확인
        Optional<UserEnv> userEnvOptional = userEnvService.findByUserId(userId);

        // builder 변수 할당
        UserProfileDto.UserProfileDtoBuilder userProfileDtoBuilder = UserProfileDto.builder();

        if (userEnvOptional.isEmpty()) {
            // env가 없으면 upbit와 binance의 wallet 정보를 받아올 수 없음.
            userProfileDtoBuilder.binanceBalance(null).upbitBalance(null);
        } else {
            // env를 통하여 각각의 거래소의 service를 생성
            UserEnv userEnv = userEnvOptional.get();
            UpbitPrivateRestService upbitPrivateRestService =
                    new UpbitPrivateRestService(
                            aesCrypto.decrypt(userEnv.getUpbitAccessKey()),
                            aesCrypto.decrypt(userEnv.getUpbitSecretKey()),
                            okHttpClient,
                            objectMapper
                    );

            BinancePrivateRestService binancePrivateRestService =
                    new BinancePrivateRestService(
                            aesCrypto.decrypt(userEnv.getBinanceAccessKey()),
                            aesCrypto.decrypt(userEnv.getBinanceSecretKey()),
                            okHttpClient,
                            objectMapper
                    );

            // 각각 거래소의 지갑정보를 받아옴
            List<UpbitGetAccountResponseDto> upbitAccountList = upbitPrivateRestService.getAccount();
            List<BinanceGetAccountResponseDto> binanceAccountList = binancePrivateRestService.getAccount();

            // 지갑 정보에서 KRW와 USDT를 필터링
            Optional<UpbitGetAccountResponseDto> upbitKRW = upbitAccountList.stream().filter(upbitAccount -> upbitAccount.getCurrency().equals("KRW")).findFirst();
            Optional<BinanceGetAccountResponseDto> binanceUSDT = binanceAccountList.stream().filter(binanceAccount -> binanceAccount.getAsset().equals("USDT")).findFirst();

            // builder에 build하기
            if (upbitKRW.isEmpty()) {
                userProfileDtoBuilder.upbitBalance(null);
            } else {
                userProfileDtoBuilder.upbitBalance(Double.parseDouble(upbitKRW.get().getBalance()));
            }

            if (binanceUSDT.isEmpty()) {
                userProfileDtoBuilder.binanceBalance(null);
            } else {
                userProfileDtoBuilder.binanceBalance(Double.parseDouble(binanceUSDT.get().getBalance()));
            }
        }

        ExchangeRateDto exchangeRateDto = exchangeRateService.getExchangeRate("USD", "KRW");

        userProfileDtoBuilder.nickname(SecurityUtil.getNickname());
        userProfileDtoBuilder.exchangeRate(exchangeRateDto.getRate());

        return userProfileDtoBuilder.build();
    }

    @Transactional
    public String updateNickname(UserEditNicknameDto req) {
        Long userId = SecurityUtil.getUserId();
        String nickname = req.getNickname();

        Optional<User> userOptional = userService.findByUserId(userId);

        if (userOptional.isEmpty()) throw new IllegalArgumentException("user is not found");

        User user = userOptional.get();
        if (user.getNickname().equals(nickname)) throw new IllegalArgumentException("Same nickname");

        user.updateUserNickname(nickname);

        return jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname(), SecurityUtil.getExpiredAt());
    }

    private UserTokenDto userTokenResponseBuilder(User user) {
        String accessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname());
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail(), UUID.randomUUID().toString());
        Long refreshTokenTTL = refreshTokenService.getRefreshTokenTTL(user.getEmail());

        return UserTokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .refreshTokenTTL(refreshTokenTTL)
                .build();
    }

    private boolean isCorrectOAuthToken(UserSignupDto req) {
        String provider = req.getProvider(); // provider is only kakao, google

        switch (provider.toLowerCase()) {
            case "google" -> {
                if (!googleApiClient.validateUser(req.getAccessToken(), req.getProviderId(), req.getEmail())) {
                    return false;
                }
            }
            case "kakao" -> {
                if (!kakaoApiClient.validateUser(req.getAccessToken(), req.getProviderId(), req.getEmail())) {
                    return false;
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    public boolean checkCode(String originCode, String encryptedCode) {
        try {
            return aesCrypto.decrypt(encryptedCode).equals(originCode);
        } catch (Exception e) {
            return false;
        }
    }


}