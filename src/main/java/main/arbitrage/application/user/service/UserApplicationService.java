package main.arbitrage.application.user.service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.domain.oauthUser.service.OAuthUserService;
import main.arbitrage.domain.userEnv.dto.UserEnvDto;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
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
    private final RefreshTokenService refreshTokenService;
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

        return AESCrypto.encrypt(code);
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
    public void registerUserEnv(UserEnvDto req) throws IOException {
        // upbit accessKey와 secretKey를 지갑 잔액을 조회함으로써 올바른 키가 맞는지 증명
        UpbitPrivateRestService upbitPrivateRestService =
                new UpbitPrivateRestService(req.getUpbitAccessKey(), req.getUpbitSecretKey(), okHttpClient, objectMapper);

        upbitPrivateRestService.getAccount();

        // binance accessKey와 secretKey를 지갑 잔액을 조회함으로써 올바른 키가 맞는지 증명
        BinancePrivateRestService binancePrivateRestService =
                new BinancePrivateRestService(req.getBinanceAccessKey(), req.getBinanceSecretKey(), okHttpClient, objectMapper);

        binancePrivateRestService.getAccount();
    }

    private UserTokenDto userTokenResponseBuilder(User user) {
        String accessToken = jwtUtil.createToken(user.getUserId(), user.getEmail());
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
            return AESCrypto.decrypt(encryptedCode).equals(originCode);
        } catch (Exception e) {
            return false;
        }
    }
}