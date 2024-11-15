package main.arbitrage.domain.price.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.application.collector.dto.ExchangePair;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.price.buffer.PriceBuffer;
import main.arbitrage.domain.price.entity.Price;
import main.arbitrage.domain.price.repository.PriceRepository;
import main.arbitrage.infrastructure.event.dto.PremiumDto;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceDomainService {
    private final PriceBuffer priceBuffer;
    private final PriceRepository symbolPriceRepository;

    public void saveBufferedData() {
        try {
            if (priceBuffer.isEmpty()) {
                return;
            }

            if (priceBuffer.isReadyToSave()) {
                var dataToSave = priceBuffer.getBufferedData();
                symbolPriceRepository.saveAll(dataToSave);
                log.info("Saved {} symbol price records to database", dataToSave.size());
                priceBuffer.clear();
            }
        } catch (Exception e) {
            log.error("Error saving priceBuffered data: ", e);
        }
    }

    public void saveToBuffer(
            String symbol,
            ExchangeRate exchangeRate,
            ExchangePair tradePair,
            PremiumDto premium
    ) {
        Price symbolPrice = Price.builder()
                .symbol(symbol)
                .exchangeRate(exchangeRate)
                .premium(premium.getPremium())
                .upbit(tradePair.getUpbit().getPrice())
                .binance(tradePair.getBinance().getPrice())
                .upbitTradeAt(tradePair.getUpbit().getTimestamp())
                .binanceTradeAt(tradePair.getBinance().getTimestamp())
                .build();

        priceBuffer.add(symbolPrice);
    }
}