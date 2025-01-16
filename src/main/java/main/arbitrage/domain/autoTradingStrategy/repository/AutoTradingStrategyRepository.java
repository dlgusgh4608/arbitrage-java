package main.arbitrage.domain.autoTradingStrategy.repository;

import main.arbitrage.domain.autoTradingStrategy.entity.AutoTradingStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutoTradingStrategyRepository extends JpaRepository<AutoTradingStrategy, Long> {}
