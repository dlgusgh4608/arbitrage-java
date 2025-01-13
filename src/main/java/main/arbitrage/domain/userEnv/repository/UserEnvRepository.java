package main.arbitrage.domain.userEnv.repository;

import main.arbitrage.domain.userEnv.entity.UserEnv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEnvRepository extends JpaRepository<UserEnv, Long> {}
