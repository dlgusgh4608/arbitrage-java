package main.arbitrage.application.price.service;

import java.util.List;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.application.price.dto.PriceDto;
import main.arbitrage.domain.price.service.PriceDomainService;

@RequiredArgsConstructor
@Service
public class PriceApplicationService {
    private final PriceDomainService priceDomainService;

    @Transactional
    public List<PriceDto> getInitialPriceOfSymbolName(String symbolName) {
        return priceDomainService.getInitialPriceOfSymbolName(symbolName).stream()
                .map(PriceDto::from).toList();
    }
}
