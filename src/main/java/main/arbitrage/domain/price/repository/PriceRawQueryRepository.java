package main.arbitrage.domain.price.repository;

import java.util.List;
import main.arbitrage.domain.price.entity.Price;

public interface PriceRawQueryRepository {
  public void bulkInsert(List<Price> prices);
}
