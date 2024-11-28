package main.arbitrage.application.user.service;

import java.util.Optional;
import java.util.UUID;

import main.arbitrage.domain.oauthUser.dto.OAuthUserRegisterRequest;
import main.arbitrage.domain.oauthUser.entity.OAuthUser;
import main.arbitrage.domain.oauthUser.service.OAuthUserService;
import main.arbitrage.infrastructure.google.GoogleApiClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.auth.jwt.JwtUtil;
import main.arbitrage.infrastructure.email.dto.EmailMessageDto;
import main.arbitrage.infrastructure.email.service.EmailMessageService;
import main.arbitrage.domain.user.dto.request.UserLoginRequest;
import main.arbitrage.domain.user.dto.request.UserRegisterRequest;
import main.arbitrage.domain.user.dto.response.UserTokenResponse;
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
    public UserTokenResponse register(UserRegisterRequest req) throws Exception {
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
    public UserTokenResponse oAuthUserRegister(OAuthUserRegisterRequest req) throws Exception {
        String provider = req.getProvider(); // provider is only kakao, google

        switch (provider.toLowerCase()) {
            case "google" -> {
                if (googleApiClient.validateUser(req.getAccessToken(), req.getProvider(), req.getEmail())) {
                    throw new IllegalArgumentException("invalid Value");
                }
            }
            case "kakao" -> {
                System.out.println(provider);
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
    public UserTokenResponse login(UserLoginRequest req) {
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

    private UserTokenResponse userTokenResponseBuilder(User user) {
        String accessToken = jwtUtil.createToken(user.getUserId(), user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail(), UUID.randomUUID().toString());
        Long refreshTokenTTL = refreshTokenService.getRefreshTokenTTL(user.getEmail());

        return UserTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .refreshTokenTTL(refreshTokenTTL)
                .build();
    }
}