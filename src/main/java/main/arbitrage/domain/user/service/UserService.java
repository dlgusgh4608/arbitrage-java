package main.arbitrage.domain.user.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.user.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User create(User user) {
        String email = user.getEmail();
        if (email == null) throw new IllegalArgumentException("Email is required!");

        String password = user.getPassword();
        if (password == null) throw new IllegalArgumentException("Password is required!");

        User Encodeuser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();

        return userRepository.save(Encodeuser);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean matchPassword(String password, String decryptedPassword) {
        return passwordEncoder.matches(password, decryptedPassword);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) throw new IllegalArgumentException("Email is required!");

        return userRepository.findByEmail(email);
    }
}