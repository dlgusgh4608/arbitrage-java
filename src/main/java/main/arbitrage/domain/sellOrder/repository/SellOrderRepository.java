package main.arbitrage.domain.sellOrder.repository;

import main.arbitrage.domain.sellOrder.entity.SellOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellOrderRepository extends JpaRepository<SellOrder, Long> {}
