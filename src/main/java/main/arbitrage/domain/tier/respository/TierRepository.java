package main.arbitrage.domain.tier.respository;

import main.arbitrage.domain.tier.entity.Tier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TierRepository extends JpaRepository<Tier, Long> {
  boolean existsByName(String name);
}
