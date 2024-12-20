package main.arbitrage.domain.userEnv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import main.arbitrage.domain.userEnv.entity.UserEnv;

public interface UserEnvRepository extends JpaRepository<UserEnv, Long> {

}
