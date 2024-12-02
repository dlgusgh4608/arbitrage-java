package main.arbitrage.domain.price.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.domain.price.buffer.PriceBuffer;
import main.arbitrage.domain.price.entity.Price;
import main.arbitrage.domain.price.repository.PriceRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceDomainService {
    private final PriceBuffer priceBuffer;
    private final PriceRepository symbolPriceRepository;

    public void saveToPG() {
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

    public void addToBuffer(
            Price price
    ) {
        priceBuffer.add(price);
    }
}