package main.arbitrage.domain.price.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.auto.dto.AutoTradingStandardValueDTO;
import main.arbitrage.application.auto.dto.QAutoTradingStandardValueDTO;
import main.arbitrage.domain.exchangeRate.entity.QExchangeRate;
import main.arbitrage.domain.price.entity.Price;
import main.arbitrage.domain.price.entity.QPrice;
import main.arbitrage.domain.symbol.entity.QSymbol;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.presentation.dto.response.ChartDataResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PriceRepositoryImpl implements PriceRawQueryRepository, PriceQueryRepository {
  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private final JPAQueryFactory queryFactory;
  private final QPrice price = QPrice.price;
  private final QSymbol symbol = QSymbol.symbol;
  private final QExchangeRate exchangeRate = QExchangeRate.exchangeRate;

  @Override
  public AutoTradingStandardValueDTO getAutoTradingStandardValue(Symbol symbol, int minutes) {
    Timestamp currentTime = Timestamp.valueOf(LocalDateTime.now().minusMinutes(minutes));

    JPAQuery<Float> avgRateSubQuery =
        queryFactory
            .select(exchangeRate.rate.avg().floatValue())
            .from(exchangeRate)
            .where(exchangeRate.createdAt.gt(currentTime));

    return queryFactory
        .select(
            new QAutoTradingStandardValueDTO(
                avgRateSubQuery,
                Expressions.numberTemplate(
                    Float.class,
                    "CAST(MAX((upbit / {0} / binance - 1) * 100) AS float)",
                    avgRateSubQuery),
                Expressions.numberTemplate(
                    Float.class,
                    "CAST(MIN((upbit / {0} / binance - 1) * 100) AS float)",
                    avgRateSubQuery)))
        .from(price)
        .where(price.createdAt.gt(currentTime))
        .where(price.symbol.eq(symbol))
        .fetchOne();
  }

  @Override
  public List<Price> findBySymbolName(String symbolName, Pageable pageable) {
    return queryFactory
        .selectFrom(price)
        .join(price.symbol, symbol)
        .where(symbol.name.eq(symbolName))
        .orderBy(price.id.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();
  }

  @Override
  public void bulkInsert(List<Price> prices) {
    String sql =
        """
        INSERT INTO price
            (
                symbol_id,
                exchange_rate_id,
                premium,
                upbit,
                binance,
                upbit_trade_at,
                binance_trade_at,
                created_at
            )
        VALUES
            (?,?,?,?,?,?,?,?)
        """;

    jdbcTemplate.batchUpdate(
        sql,
        prices,
        prices.size(),
        (PreparedStatement ps, Price price) -> {
          ps.setLong(1, price.getSymbol().getId());
          ps.setLong(2, price.getExchangeRate().getId());
          ps.setFloat(3, price.getPremium());
          ps.setDouble(4, price.getUpbit());
          ps.setDouble(5, price.getBinance());
          ps.setTimestamp(6, price.getUpbitTradeAt());
          ps.setTimestamp(7, price.getBinanceTradeAt());
          ps.setTimestamp(8, price.getCreatedAt());
        });
  }

  @Override
  public List<ChartDataResponse> getPremiumOHLC(Symbol symbol, int unit, long lastTime) {
    long intervalSeconds = unit * 60L;
    Timestamp lastTimestamp = new Timestamp(lastTime);

    String sql =
        """
        SELECT
          time_bucket,
          MAX(open_price) AS open,
          MAX(premium) AS high,
          MIN(premium) AS low,
          MAX(close_price) AS close
        FROM (
          SELECT
            FLOOR(EXTRACT(EPOCH FROM created_at)/:interval) * :interval AS time_bucket,
            premium,
            FIRST_VALUE(premium) OVER w AS open_price,
            LAST_VALUE(premium) OVER w AS close_price
          FROM price
          WHERE symbol_id = :symbolId
            AND created_at < :lastTime
          WINDOW w AS (
            PARTITION BY FLOOR(EXTRACT(EPOCH FROM created_at)/:interval) * :interval
            ORDER BY created_at
            RANGE BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
          )
        ) sub
        GROUP BY time_bucket
        ORDER BY time_bucket DESC
        LIMIT 300
        """;

    return namedParameterJdbcTemplate.query(
        sql,
        Map.of(
            "interval", intervalSeconds,
            "symbolId", symbol.getId(),
            "lastTime", lastTimestamp),
        (rs, rowNum) ->
            ChartDataResponse.builder()
                .x(rs.getLong("time_bucket"))
                .o(rs.getFloat("open"))
                .h(rs.getFloat("high"))
                .l(rs.getFloat("low"))
                .c(rs.getFloat("close"))
                .build());
  }
}
