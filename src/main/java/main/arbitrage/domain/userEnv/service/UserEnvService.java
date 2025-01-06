package main.arbitrage.domain.userEnv.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.userEnv.entity.UserEnv;
import main.arbitrage.domain.userEnv.exception.UserEnvErrorCode;
import main.arbitrage.domain.userEnv.exception.UserEnvException;
import main.arbitrage.domain.userEnv.repository.UserEnvRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEnvService {
  private final UserEnvRepository userEnvRepository;

  public Optional<UserEnv> findByUserId(Long userId) {
    try {
      return userEnvRepository.findById(userId);
    } catch (Exception e) {
      throw new UserEnvException(UserEnvErrorCode.UNKNOWN, e);
    }
  }

  public UserEnv findAndExistByUserId(Long userId) {
    try {

      return userEnvRepository
          .findById(userId)
          .orElseThrow(() -> new UserEnvException(UserEnvErrorCode.NOT_FOUND_USER_ENV));

    } catch (UserEnvException e) {
      throw e;
    } catch (Exception e) {
      throw new UserEnvException(UserEnvErrorCode.UNKNOWN, e);
    }
  }

  public void create(UserEnv userEnv) {
    try {
      userEnvRepository.save(userEnv);
    } catch (Exception e) {
      throw new UserEnvException(UserEnvErrorCode.UNKNOWN, e);
    }
  }
}
