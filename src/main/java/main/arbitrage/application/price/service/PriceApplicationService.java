package main.arbitrage.application.price.service;

import java.util.List;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.price.service.PriceDomainService;
import main.arbitrage.presentation.dto.view.PriceView;

@RequiredArgsConstructor
@Service
public class PriceApplicationService {
    private final PriceDomainService priceDomainService;

    @Transactional
    public List<PriceView> getInitialPriceOfSymbolName(String symbolName) {
        return priceDomainService.getInitialPriceOfSymbolName(symbolName).stream()
                .map(PriceView::from).toList();
    }
}
