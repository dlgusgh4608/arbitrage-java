package main.arbitrage.domain.buyOrder.repository;

import java.util.List;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.symbol.entity.Symbol;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuyOrderRepository extends JpaRepository<BuyOrder, Long>, BuyOrderQueryRepository {
  List<BuyOrder> findByUserIdAndSymbolAndIsCloseFalseOrderByCreatedAtDesc(
      Long userId, Symbol symbol);

  @Override
  List<BuyOrder> findBuyOrdersByUserIdAndSymbolId(long userId, long symbolId, Pageable pageable);
}
