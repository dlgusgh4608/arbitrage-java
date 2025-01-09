package main.arbitrage.domain.buyOrder.repository;

import java.util.List;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.symbol.entity.Symbol;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyOrderRepository extends JpaRepository<BuyOrder, Long> {
  List<BuyOrder> findByUserIdAndSymbolAndIsCloseFalseOrderByCreatedAtDesc(
      Long userId, Symbol symbol);

  List<BuyOrder> findByUserIdAndSymbolOrderByCreatedAtDesc(
      Long userId, Symbol symbol, Pageable pageable);
}
