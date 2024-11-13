package main.arbitrage.domain.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.user.dto.request.UserLoginRequest;
import main.arbitrage.domain.user.dto.request.UserRegisterRequest;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.user.repository.UserRepository;
import main.arbitrage.global.auth.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public User register(UserRegisterRequest request) {

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

        return userRepository.save(user);
    }

    @Transactional
    public String login(UserLoginRequest request) {
        String email = request.getEmail();
        if (email == null) throw new IllegalArgumentException("Email is required!");

        String password = request.getPassword();
        if (password == null) throw new IllegalArgumentException("Password is required!");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid info"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid info");
        }

        return jwtProvider.createToken(email);
    }
}