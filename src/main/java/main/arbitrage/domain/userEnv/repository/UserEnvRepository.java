package main.arbitrage.domain.userEnv.repository;

import main.arbitrage.domain.userEnv.entity.UserEnv;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEnvRepository extends JpaRepository<UserEnv, Long> {}
