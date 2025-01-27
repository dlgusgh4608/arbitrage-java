package main.arbitrage.domain.price.repository;

import java.util.List;
import main.arbitrage.application.auto.dto.AutoTradingStandardValueDTO;
import main.arbitrage.domain.price.entity.Price;
import main.arbitrage.domain.symbol.entity.Symbol;
import org.springframework.data.domain.Pageable;

public interface PriceQueryRepository {
  public List<Price> findBySymbolName(String symbolName, Pageable pageable);

  public AutoTradingStandardValueDTO getAutoTradingStandardValue(Symbol symbol, int minutes);
}
