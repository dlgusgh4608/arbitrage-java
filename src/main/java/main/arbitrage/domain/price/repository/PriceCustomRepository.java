package main.arbitrage.domain.price.repository;

import java.sql.PreparedStatement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.price.entity.Price;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PriceCustomRepository {
  private final JdbcTemplate jdbcTemplate;

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
}
