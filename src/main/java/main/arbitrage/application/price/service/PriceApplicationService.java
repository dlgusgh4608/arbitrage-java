package main.arbitrage.application.price.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.price.service.PriceService;
import main.arbitrage.domain.symbol.entity.Symbol;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.presentation.dto.response.ChartDataResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PriceApplicationService {
  private final PriceService priceService;
  private final SymbolVariableService symbolVariableService;

  @Transactional
  public List<ChartDataResponse> getOHLC(String symbolName, int intervalMinutes, long lastTime) {
    Symbol symbol = symbolVariableService.findAndExistSymbolByName(symbolName);
    return priceService.getPremiumOHLC(symbol, intervalMinutes, lastTime);
  }
}
