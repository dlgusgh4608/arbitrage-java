package main.arbitrage.domain.userEnv.repository;

import java.util.List;
import main.arbitrage.application.auto.dto.AutomaticUserInfoDTO;

public interface UserEnvQueryRepository {
  List<AutomaticUserInfoDTO> findAutomaticUsers();
}
