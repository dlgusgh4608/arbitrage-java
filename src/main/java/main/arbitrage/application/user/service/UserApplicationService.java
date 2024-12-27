package main.arbitrage.application.user.service;

import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.exchangeRate.dto.ExchangeRateDTO;
import main.arbitrage.auth.jwt.JwtUtil;
import main.arbitrage.auth.security.SecurityUtil;
import main.arbitrage.domain.exchangeRate.service.ExchangeRateService;
import main.arbitrage.domain.oauthUser.service.OAuthUserService;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.user.service.UserService;
import main.arbitrage.domain.userEnv.entity.UserEnv;
import main.arbitrage.domain.userEnv.service.UserEnvService;
import main.arbitrage.global.util.aes.AESCrypto;
import main.arbitrage.infrastructure.email.dto.EmailMessageDTO;
import main.arbitrage.infrastructure.email.service.EmailMessageService;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceGetAccountResponse;
import main.arbitrage.infrastructure.exchange.dto.ExchangePrivateRestPair;
import main.arbitrage.infrastructure.exchange.factory.ExchangePrivateRestFactory;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitGetAccountResponse;
import main.arbitrage.infrastructure.oauthValidator.google.GoogleApiClient;
import main.arbitrage.infrastructure.oauthValidator.kakao.KakaoApiClient;
import main.arbitrage.infrastructure.redis.service.RefreshTokenService;
import main.arbitrage.presentation.dto.form.UserEnvForm;
import main.arbitrage.presentation.dto.form.UserLoginForm;
import main.arbitrage.presentation.dto.form.UserSignupForm;
import main.arbitrage.presentation.dto.request.EditUserNicknameRequest;
import main.arbitrage.presentation.dto.response.UserTokenResponseCookie;
import main.arbitrage.presentation.dto.view.UserProfileView;


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

    private final GoogleApiClient googleApiClient;
    private final KakaoApiClient kakaoApiClient;

    private final ExchangePrivateRestFactory exchangePrivateRestFactory;

    @Transactional
    public String sendEmail(String email) throws Exception {
        if (userService.existsByEmail(email)) {
            throw new DataIntegrityViolationException("This email is already in use: " + email);
        }

        String code = emailMessageService.sendMail(EmailMessageDTO.builder().to(email)
                .subject("[Arbitrage] 이메일 인증을 위한 인증 코드 발송").build(), "email");

        return aesCrypto.encrypt(code.getBytes());
    }

    @Transactional
    public UserTokenResponseCookie signup(UserSignupForm req) {
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
            if (!checkCode(code, encryptedCode))
                throw new IllegalArgumentException("Invalid value");

            return userTokenResponseBuilder(userService.create(req));
        } else if (!accessToken.isEmpty() && !provider.isEmpty() && !providerId.isEmpty()) {
            if (!isCorrectOAuthToken(req))
                throw new IllegalArgumentException("Invalid value");

            User user = userService.create(req);
            oAuthUserService.create(user, req);

            return userTokenResponseBuilder(user);
        } else {
            throw new IllegalArgumentException("Invalid value");
        }
    }

    @Transactional
    public UserTokenResponseCookie login(UserLoginForm req) {
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
    public void registerUserEnv(UserEnvForm req) throws Exception {
        Long userId = SecurityUtil.getUserId();

        Optional<User> optionalUser = userService.findByEmail(SecurityUtil.getEmail());

        if (optionalUser.isEmpty())
            throw new IllegalArgumentException("user is not found");

        // upbit accessKey와 secretKey를 지갑 잔액을 조회함으로써 올바른 키가 맞는지 증명
        ExchangePrivateRestPair upbitExchangePrivateRestPair =
                exchangePrivateRestFactory.create(req.getUpbitAccessKey(), req.getUpbitSecretKey(),
                        req.getBinanceAccessKey(), req.getBinanceSecretKey());

        upbitExchangePrivateRestPair.getUpbit().getAccount();
        upbitExchangePrivateRestPair.getBinance().getAccount();

        Optional<UserEnv> optionalUserEnv = userEnvService.findByUserId(userId);

        if (optionalUserEnv.isEmpty()) {
            userEnvService.create(UserEnvForm.toEntity(req, optionalUser.get(), aesCrypto));
        } else {
            UserEnv userEnv = optionalUserEnv.get();
            userEnv.updateEnv(req, aesCrypto);
        }
    }

    @Transactional
    public UserProfileView getUserProfile() throws Exception {
        Long userId = SecurityUtil.getUserId();

        // userId에 해당하는 env가 있는지 확인
        Optional<UserEnv> userEnvOptional = userEnvService.findByUserId(userId);

        // builder 변수 할당
        UserProfileView.UserProfileViewBuilder userProfileDtoBuilder = UserProfileView.builder();

        if (userEnvOptional.isEmpty()) {
            // env가 없으면 upbit와 binance의 wallet 정보를 받아올 수 없음.
            userProfileDtoBuilder.binanceBalance(null).upbitBalance(null);
        } else {
            // env를 통하여 거래소의 service를 생성
            UserEnv userEnv = userEnvOptional.get();
            ExchangePrivateRestPair upbitExchangePrivateRestPair =
                    exchangePrivateRestFactory.create(userEnv);

            // 각각 거래소의 지갑정보를 받아옴
            Optional<UpbitGetAccountResponse> upbitKRW =
                    upbitExchangePrivateRestPair.getUpbit().getKRW();
            Optional<BinanceGetAccountResponse> binanceUSDT =
                    upbitExchangePrivateRestPair.getBinance().getUSDT();

            // builder에 build하기
            if (upbitKRW.isEmpty()) {
                userProfileDtoBuilder.upbitBalance(null);
            } else {
                userProfileDtoBuilder.upbitBalance(Double.parseDouble(upbitKRW.get().balance()));
            }

            if (binanceUSDT.isEmpty()) {
                userProfileDtoBuilder.binanceBalance(null);
            } else {
                userProfileDtoBuilder
                        .binanceBalance(Double.parseDouble(binanceUSDT.get().balance()));
            }
        }

        ExchangeRateDTO exchangeRateDto = exchangeRateService.getExchangeRate("USD", "KRW");

        userProfileDtoBuilder.nickname(SecurityUtil.getNickname());
        userProfileDtoBuilder.exchangeRate(exchangeRateDto.getRate());

        return userProfileDtoBuilder.build();
    }

    @Transactional
    public String updateNickname(EditUserNicknameRequest req) {
        Long userId = SecurityUtil.getUserId();
        String nickname = req.nickname();

        Optional<User> userOptional = userService.findByUserId(userId);

        if (userOptional.isEmpty())
            throw new IllegalArgumentException("user is not found");

        User user = userOptional.get();
        if (user.getNickname().equals(nickname))
            throw new IllegalArgumentException("Same nickname");

        user.updateUserNickname(nickname);

        return jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname(),
                SecurityUtil.getExpiredAt());
    }

    private UserTokenResponseCookie userTokenResponseBuilder(User user) {
        String accessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getNickname());
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail(),
                UUID.randomUUID().toString());
        Long refreshTokenTTL = refreshTokenService.getRefreshTokenTTL(user.getEmail());

        return UserTokenResponseCookie.builder().accessToken(accessToken).refreshToken(refreshToken)
                .refreshTokenTTL(refreshTokenTTL).build();
    }

    private boolean isCorrectOAuthToken(UserSignupForm req) {
        String provider = req.getProvider(); // provider is only kakao, google

        switch (provider.toLowerCase()) {
            case "google" -> {
                if (!googleApiClient.validateUser(req.getAccessToken(), req.getProviderId(),
                        req.getEmail())) {
                    return false;
                }
            }
            case "kakao" -> {
                if (!kakaoApiClient.validateUser(req.getAccessToken(), req.getProviderId(),
                        req.getEmail())) {
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
