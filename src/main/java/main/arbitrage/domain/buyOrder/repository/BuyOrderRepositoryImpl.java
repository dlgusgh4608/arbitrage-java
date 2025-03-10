package main.arbitrage.domain.buyOrder.repository;

import com.querydsl.jpa.impl.JPAQuery;
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
  private final QExchangeRate exchangeRate = QExchangeRate.exchangeRate;

  private JPAQuery<BuyOrder> findByOrderQuery(long userId) {
    return queryFactory
        .selectFrom(buyOrder)
        .innerJoin(buyOrder.exchangeRate, exchangeRate)
        .fetchJoin()
        .where(buyOrder.userId.eq(userId))
        .orderBy(buyOrder.createdAt.desc());
  }

  private void appendSellOrders(List<BuyOrder> buyOrders) {
    for (BuyOrder buyOrder : buyOrders) {
      buyOrder.setSellOrder(
          queryFactory
              .selectFrom(sellOrder)
              .innerJoin(sellOrder.exchangeRate, exchangeRate)
              .fetchJoin()
              .where(sellOrder.buyOrder.id.eq(buyOrder.getId()))
              .orderBy(sellOrder.createdAt.desc())
              .fetch());
    }
  }

  @Override
  public List<BuyOrder> findBuyOrdersByUserIdAndSymbolId(
      long userId, long symbolId, Pageable pageable) {
    List<BuyOrder> buyOrders =
        findByOrderQuery(userId)
          .where(buyOrder.symbol.id.eq(symbolId))
          .offset(pageable.getOffset())
          .limit(pageable.getPageSize())
          .fetch();

    appendSellOrders(buyOrders);

    return buyOrders;
  }

  @Override
  public List<BuyOrder> findBuyOrderByUserId(long userId, Pageable pageable) {
    List<BuyOrder> buyOrders = findByOrderQuery(userId)
      .offset(pageable.getOffset())
      .limit(pageable.getPageSize())
      .fetch();
    appendSellOrders(buyOrders);

    return buyOrders;
  }

  @Override
  public List<BuyOrder> findBuyOrderByUserIdAndSymbolIdAndIsCloseFalse(long userId, long symbolId) {
    List<BuyOrder> buyOrders = findByOrderQuery(userId).where(buyOrder.symbol.id.eq(symbolId)).where(buyOrder.isClose.isFalse()).fetch();
    appendSellOrders(buyOrders);
    
    return buyOrders;
  }
}
