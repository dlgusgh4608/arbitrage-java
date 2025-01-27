package main.arbitrage.application.price.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.price.service.PriceService;
import main.arbitrage.presentation.dto.view.PriceView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PriceApplicationService {
  private final PriceService priceService;

  @Transactional
  public List<PriceView> getInitialPriceOfSymbolName(String symbolName) {
    return priceService.getInitialPriceOfSymbolName(symbolName).stream()
        .map(PriceView::fromEntity)
        .toList();
  }
}
