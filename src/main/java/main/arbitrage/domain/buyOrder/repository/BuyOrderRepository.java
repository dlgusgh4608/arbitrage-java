package main.arbitrage.domain.buyOrder.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.user.entity.User;


public interface BuyOrderRepository extends JpaRepository<BuyOrder, Long> {
    List<BuyOrder> findByUserAndIsCloseFalse(User user);

    List<BuyOrder> findByUserAndSymbol(User user, Symbol symbol);
}
