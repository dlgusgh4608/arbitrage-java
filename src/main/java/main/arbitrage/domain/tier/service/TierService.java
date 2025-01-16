package main.arbitrage.domain.tier.service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.tier.entity.Tier;
import main.arbitrage.domain.tier.exception.TierErrorCode;
import main.arbitrage.domain.tier.exception.TierException;
import main.arbitrage.domain.tier.respository.TierRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TierService {
  private final List<String> DEFAULT_TIER =
      List.of("BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "MASTER");

  private final TierRepository tierRepository;

  @PostConstruct
  private void init() {
    initializeTiers();
  }

  private void initializeTiers() {
    DEFAULT_TIER.forEach(this::initializeTier);
  }

  private void initializeTier(String name) {
    try {
      if (!tierRepository.existsByName(name)) {
        tierRepository.save(Tier.builder().name(name).build());
      }
    } catch (Exception e) {
      throw new TierException(
          TierErrorCode.INITIALIZED_FAILED, String.format("티어 '%s' 초기화 중 오류 발생", name), e);
    }
  }
}
