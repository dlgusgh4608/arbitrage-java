package main.arbitrage.domain.price.repository;

import java.util.List;
import main.arbitrage.application.auto.dto.AutoTradingStandardValueDTO;
import main.arbitrage.domain.price.entity.Price;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.presentation.dto.response.ChartDataResponse;
import org.springframework.data.domain.Pageable;

public interface PriceQueryRepository {
  public List<Price> findBySymbolName(String symbolName, Pageable pageable);

  public AutoTradingStandardValueDTO getAutoTradingStandardValue(Symbol symbol, int minutes);

  public List<ChartDataResponse> getPremiumOHLC(Symbol symbol, int unit, long lastTime);
}
