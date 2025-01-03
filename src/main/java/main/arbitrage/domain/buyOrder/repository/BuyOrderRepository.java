package main.arbitrage.domain.buyOrder.repository;

import java.util.List;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyOrderRepository extends JpaRepository<BuyOrder, Long> {
  List<BuyOrder> findByUserAndSymbolAndIsCloseFalse(User user, Symbol symbol);

  List<BuyOrder> findByUserAndSymbol(User user, Symbol symbol);
}
