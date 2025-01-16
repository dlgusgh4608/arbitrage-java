package main.arbitrage.domain.tier.repository;

import java.util.Optional;
import main.arbitrage.domain.tier.entity.Tier;
import main.arbitrage.domain.tier.entity.TierName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TierRepository extends JpaRepository<Tier, Long> {
  boolean existsByName(TierName name);

  Optional<Tier> findByName(TierName name);
}
