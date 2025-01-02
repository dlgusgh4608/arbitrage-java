package main.arbitrage.domain.sellOrder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import main.arbitrage.domain.sellOrder.entity.SellOrder;

public interface SellOrderRepository extends JpaRepository<SellOrder, Long> {

}
