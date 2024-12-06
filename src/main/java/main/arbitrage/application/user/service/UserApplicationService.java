package main.arbitrage.application.user.service;

import java.util.Optional;
import java.util.UUID;

import main.arbitrage.domain.oauthUser.dto.OAuthUserRegisterRequest;
import main.arbitrage.domain.oauthUser.entity.OAuthUser;
import main.arbitrage.domain.oauthUser.service.OAuthUserService;
import main.arbitrage.infrastructure.oauthValidator.google.GoogleApiClient;
import main.arbitrage.infrastructure.oauthValidator.kakao.KakaoApiClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.auth.jwt.JwtUtil;
import main.arbitrage.infrastructure.email.dto.EmailMessageDto;
import main.arbitrage.infrastructure.email.service.EmailMessageService;
import main.arbitrage.domain.user.dto.UserLoginDto;
import main.arbitrage.domain.user.dto.UserRegisterDto;
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

    private final AESCrypto aesCrypto;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    private final GoogleApiClient googleApiClient;
    private final KakaoApiClient kakaoApiClient;

    @Transactional
    public String sendEmail(EmailMessageDto emailMessageDto) throws Exception {
        String email = emailMessageDto.getTo();

        if (userService.existsByEmail(email)) {
            throw new DataIntegrityViolationException("This email is already in use: " + email);
        }

        String code = emailMessageService.sendMail(emailMessageDto, "email");

        return aesCrypto.encrypt(code);
    }

    public boolean checkCode(String originCode, String encryptedCode) throws Exception {
        return aesCrypto.decrypt(encryptedCode).equals(originCode);
    }

    @Transactional
    public UserTokenDto register(UserRegisterDto req) throws Exception {
        if (userService.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("This email is already in use");
        }

        if (!checkCode(req.getCode(), req.getEncryptedCode())) {
            throw new IllegalArgumentException("Invalid Code");
        }

        User user = userService.create(User.builder()
                .email(req.getEmail())
                .password(req.getPassword())
                .build()
        );

        return userTokenResponseBuilder(user);
    }

    @Transactional
    public UserTokenDto oAuthUserRegister(OAuthUserRegisterRequest req) throws Exception {
        String provider = req.getProvider(); // provider is only kakao, google

        System.out.println(provider);

        switch (provider.toLowerCase()) {
            case "google" -> {
                if (!googleApiClient.validateUser(req.getAccessToken(), req.getProviderId(), req.getEmail())) {
                    System.out.println(1);
                    throw new IllegalArgumentException("invalid Value");
                }
            }
            case "kakao" -> {
                if (!kakaoApiClient.validateUser(req.getAccessToken(), req.getProviderId(), req.getEmail())) {
                    System.out.println(2);
                    throw new IllegalArgumentException("invalid Value");
                }
            }
            default -> throw new Exception("invalid provider");
        }

        User user = userService.create(User.builder()
                .email(req.getEmail())
                .password(req.getPassword())
                .build()
        );

        oAuthUserService.create(OAuthUser.builder()
                .providerId(req.getProviderId())
                .provider(provider)
                .user(user)
                .build()
        );

        return userTokenResponseBuilder(user);
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
}