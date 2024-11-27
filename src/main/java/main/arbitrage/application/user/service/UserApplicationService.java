package main.arbitrage.application.user.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.auth.jwt.JwtUtil;
import main.arbitrage.domain.email.entity.EmailMessage;
import main.arbitrage.domain.email.service.EmailMessageService;
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
    private final AESCrypto aesCrypto;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    public String sendEmail(EmailMessage emailMessage) throws Exception {
        String code = emailMessageService.sendMail(emailMessage, "email");
        return aesCrypto.encrypt(code);
    }

    public boolean checkCode(String originCode, String encryptedCode) throws Exception {
        return aesCrypto.decrypt(encryptedCode).equals(originCode);
    }

    public void validateEmail(String email) {
        if (userService.existsByEmail(email)) {
            throw new DataIntegrityViolationException("This email is already in use: " + email);
        }
    }

    @Transactional
    public UserTokenResponse register(UserRegisterRequest req) throws Exception {
        if (userService.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("This email is already in use");
        }

        if (!checkCode(req.getCode(), req.getEncryptedCode())) {
            throw new IllegalArgumentException("Invalid Code");
        }

        User user = userService.create(req);

        String accessToken = jwtUtil.createToken(user.getUserId(), user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail(), UUID.randomUUID().toString());
        Long refreshTokenTTL = refreshTokenService.getRefreshTokenTTL(user.getEmail());

        return UserTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .refreshTokenTTL(refreshTokenTTL)
                .build();
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