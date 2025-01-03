package main.arbitrage.domain.userEnv.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.userEnv.entity.UserEnv;
import main.arbitrage.domain.userEnv.repository.UserEnvRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEnvService {
  private final UserEnvRepository userEnvRepository;

  public Optional<UserEnv> findByUserId(Long userId) {
    return userEnvRepository.findById(userId);
  }

  public void create(UserEnv userEnv) {
    userEnvRepository.save(userEnv);
  }

  public void update(UserEnv userEnv) {
    userEnvRepository.save(userEnv);
  }
}
