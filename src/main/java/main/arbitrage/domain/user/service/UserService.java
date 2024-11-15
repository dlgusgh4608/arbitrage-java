package main.arbitrage.domain.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.user.dto.request.UserLoginRequest;
import main.arbitrage.domain.user.dto.request.UserRegisterRequest;
import main.arbitrage.domain.user.dto.response.UserLoginResponse;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.user.repository.UserRepository;
import main.arbitrage.auth.jwt.JwtUtil;
import main.arbitrage.infrastructure.redis.service.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtProvider;


    @Transactional
    public void register(UserRegisterRequest request) {

        String email = request.getEmail();
        if (email == null) throw new IllegalArgumentException("Email is required!");

        String password = request.getPassword();
        if (password == null) throw new IllegalArgumentException("Password is required!");

        String nickname = request.getNickname();
        if (nickname == null) throw new IllegalArgumentException("Nickname is required!");

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("This email is already in use");
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("This nickname is already in use");
        }

        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password(passwordEncoder.encode(password))
                .build();

        userRepository.save(user);
    }

    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        String email = request.getEmail();
        if (email == null) throw new IllegalArgumentException("Email is required!");

        String password = request.getPassword();
        if (password == null) throw new IllegalArgumentException("Password is required!");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid info"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid info");
        }

        String accessToken = jwtProvider.createToken(user.getUserId(), email, user.getNickname());
        String refreshToken = refreshTokenService.createRefreshToken(email, UUID.randomUUID().toString());

        return UserLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}