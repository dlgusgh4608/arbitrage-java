package main.arbitrage.domain.buyOrder.repository;

import java.util.List;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import org.springframework.data.domain.Pageable;

public interface BuyOrderQueryRepository {
  public List<BuyOrder> findBuyOrdersByUserIdAndSymbolId(
      long userId, long symbolId, Pageable pageable);
}