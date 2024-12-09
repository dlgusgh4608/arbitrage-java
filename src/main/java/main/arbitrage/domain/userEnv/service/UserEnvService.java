package main.arbitrage.domain.userEnv.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.userEnv.repository.UserEnvRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEnvService {
    private final UserEnvRepository userEnvRepository;

    public boolean existsByUserId(Long userId) {
        return userEnvRepository.existsById(userId);
    }
}