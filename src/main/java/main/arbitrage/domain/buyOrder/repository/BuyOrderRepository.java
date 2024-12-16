package main.arbitrage.domain.buyOrder.repository;

import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyOrderRepository extends JpaRepository<BuyOrder, Long> {
    
}