package main.arbitrage.domain.user.service;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.user.repository.UserRepository;
import main.arbitrage.presentation.dto.form.UserSignupForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User create(UserSignupForm userSignupDto) {
    User Encodeuser =
        User.builder()
            .email(userSignupDto.getEmail())
            .nickname(UUID.randomUUID().toString())
            .password(passwordEncoder.encode(userSignupDto.getPassword()))
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

  public Optional<User> findByUserId(Long userId) {
    return userRepository.findById(userId);
  }
}
