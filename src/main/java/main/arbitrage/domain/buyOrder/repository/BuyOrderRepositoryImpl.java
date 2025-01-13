package main.arbitrage.domain.buyOrder.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.buyOrder.entity.BuyOrder;
import main.arbitrage.domain.buyOrder.entity.QBuyOrder;
import main.arbitrage.domain.exchangeRate.entity.QExchangeRate;
import main.arbitrage.domain.sellOrder.entity.QSellOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BuyOrderRepositoryImpl implements BuyOrderQueryRepository {
  private final JPAQueryFactory queryFactory;
  private final QBuyOrder buyOrder = QBuyOrder.buyOrder;
  private final QSellOrder sellOrder = QSellOrder.sellOrder;
  private final QExchangeRate buyExchangeRate = new QExchangeRate("buyExchangeRate");
  private final QExchangeRate sellExchangeRate = new QExchangeRate("sellExchangeRate");

  @Override
  public List<BuyOrder> findBuyOrdersByUserIdAndSymbolId(
      long userId, long symbolId, Pageable pageable) {
    return queryFactory
        .selectFrom(buyOrder)
        .leftJoin(buyOrder.sellOrders, sellOrder)
        .fetchJoin()
        .innerJoin(buyOrder.exchangeRate, buyExchangeRate)
        .fetchJoin()
        .innerJoin(sellOrder.exchangeRate, sellExchangeRate)
        .fetchJoin()
        .where(buyOrder.userId.eq(userId).and(buyOrder.symbol.id.eq(symbolId)))
        .orderBy(buyOrder.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();
  }
}
