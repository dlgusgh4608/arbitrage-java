package main.arbitrage.domain.tier.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.tier.entity.Tier;
import main.arbitrage.domain.tier.entity.TierName;
import main.arbitrage.domain.tier.exception.TierErrorCode;
import main.arbitrage.domain.tier.exception.TierException;
import main.arbitrage.domain.tier.repository.TierRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TierService {
  private final TierRepository tierRepository;

  @PostConstruct
  private void init() {
    for (TierName name : TierName.values()) {
      try {
        if (!tierRepository.existsByName(name)) {
          tierRepository.save(Tier.builder().name(name).build());
        }
      } catch (Exception e) {
        throw new TierException(
            TierErrorCode.INITIALIZED_FAILED, String.format("티어 '%s' 초기화 중 오류 발생", name.name()), e);
      }
    }
  }

  public Tier getDefaultTier() {
    try {
      return tierRepository
          .findByName(TierName.BRONZE)
          .orElseThrow(() -> new TierException(TierErrorCode.NOT_FOUND));
    } catch (TierException e) {
      throw e;
    } catch (Exception e) {
      throw new TierException(TierErrorCode.UNKNOWN, e);
    }
  }
}
