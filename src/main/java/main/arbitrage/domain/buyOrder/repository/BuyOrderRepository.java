package main.arbitrage.domain.buyOrder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;

public interface BuyOrderRepository extends JpaRepository<BuyOrder, Long> {

}
