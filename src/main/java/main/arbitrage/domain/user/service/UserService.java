package main.arbitrage.domain.user.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.grade.entity.Grade;
import main.arbitrage.domain.tier.entity.Tier;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.user.exception.UserErrorCode;
import main.arbitrage.domain.user.exception.UserException;
import main.arbitrage.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User create(String email, String password, Grade grade, Tier tier) {
    try {
      User Encodeuser =
          User.builder()
              .email(email)
              .nickname(UUID.randomUUID().toString())
              .password(passwordEncoder.encode(password))
              .grade(grade)
              .tier(tier)
              .build();

      return userRepository.save(Encodeuser);
    } catch (Exception e) {
      throw new UserException(UserErrorCode.UNKNOWN, e);
    }
  }

  public void existsByEmail(String email) {
    try {
      if (userRepository.existsByEmail(email))
        throw new UserException(UserErrorCode.USED_EMAIL, email);
    } catch (UserException e) {
      throw e;
    } catch (Exception e) {
      throw new UserException(UserErrorCode.UNKNOWN, e);
    }
  }

  public void matchPassword(String password, String decryptedPassword) {
    try {
      if (!passwordEncoder.matches(password, decryptedPassword))
        throw new UserException(UserErrorCode.INVALID_PASSWORD);
    } catch (UserException e) {
      throw e;
    } catch (Exception e) {
      throw new UserException(UserErrorCode.UNKNOWN, e);
    }
  }

  public User findAndExistByEmail(String email) {
    try {
      return userRepository
          .findByEmail(email)
          .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));
    } catch (UserException e) {
      throw e;
    } catch (Exception e) {
      throw new UserException(UserErrorCode.UNKNOWN, e);
    }
  }

  public User findAndExistByUserId(Long userId) {
    try {
      return userRepository
          .findById(userId)
          .orElseThrow(() -> new UserException(UserErrorCode.NOT_FOUND_USER));
    } catch (UserException e) {
      throw e;
    } catch (Exception e) {
      throw new UserException(UserErrorCode.UNKNOWN, e);
    }
  }

  public Optional<User> findByEmail(String email) {
    try {
      return userRepository.findByEmail(email);
    } catch (Exception e) {
      throw new UserException(UserErrorCode.UNKNOWN, e);
    }
  }

  public Optional<User> findByUserId(Long userId) {
    try {
      return userRepository.findById(userId);
    } catch (Exception e) {
      throw new UserException(UserErrorCode.UNKNOWN, e);
    }
  }

  public void updateNickname(User user, String nickname) {
    try {
      if (userRepository.existsByNickname(nickname))
        throw new UserException(UserErrorCode.USED_NICKNAME);
      user.updateUserNickname(nickname);
    } catch (UserException e) {
      throw e;
    } catch (Exception e) {
      throw new UserException(UserErrorCode.UNKNOWN, e);
    }
  }

  public List<User> findAll() {
    return userRepository.findAll();
  }
}
