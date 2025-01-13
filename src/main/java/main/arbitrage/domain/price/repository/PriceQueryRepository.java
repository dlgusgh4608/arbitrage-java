package main.arbitrage.domain.price.repository;

import java.util.List;
import main.arbitrage.domain.price.entity.Price;
import org.springframework.data.domain.Pageable;

public interface PriceQueryRepository {
  public List<Price> findBySymbolName(String symbolName, Pageable pageable);
}
