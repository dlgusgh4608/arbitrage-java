package main.arbitrage.domain.tradingStrategy.repository;

import main.arbitrage.domain.tradingStrategy.entity.TradingStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradingStrategyRepository extends JpaRepository<TradingStrategy, Long> {}
