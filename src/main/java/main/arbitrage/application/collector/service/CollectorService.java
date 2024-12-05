package main.arbitrage.application.collector.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.price.entity.Price;
import main.arbitrage.domain.price.service.PriceDomainService;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CollectorService {
    private final PriceDomainService priceDomainService;

    @Transactional
    public List<Price> getInitialPriceOfSymbolName(String symbolName) {
        return priceDomainService.getInitialPriceOfSymbolName(symbolName);
    }
}
