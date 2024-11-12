package main.arbitrage.domain.price.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.domain.price.buffer.PriceBuffer;
import main.arbitrage.domain.price.repository.PriceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceBatchService {
    private final PriceBuffer buffer;
    private final PriceRepository symbolPriceRepository;

    @Scheduled(fixedDelay = 60 * 1000)
    @Transactional
    public void saveBufferedData() {
        try {
            if (buffer.isEmpty()) {
                return;
            }

            if (buffer.isReadyToSave()) {
                var dataToSave = buffer.getBufferedData();
                symbolPriceRepository.saveAll(dataToSave);
                log.info("Saved {} symbol price records to database", dataToSave.size());
                buffer.clear();
            }
        } catch (Exception e) {
            log.error("Error saving buffered data: ", e);
        }
    }
}