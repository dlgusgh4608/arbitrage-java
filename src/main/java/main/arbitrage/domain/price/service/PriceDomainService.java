package main.arbitrage.domain.price.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.domain.price.buffer.PriceBuffer;
import main.arbitrage.domain.price.entity.Price;
import main.arbitrage.domain.price.repository.PriceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceDomainService {
  private final PriceBuffer priceBuffer;
  private final PriceRepository priceRepository;

  public void saveToPG() {
    try {
      if (priceBuffer.isEmpty()) {
        return;
      }

      if (priceBuffer.isReadyToSave()) {
        var dataToSave = priceBuffer.getBufferedData();
        priceRepository.saveAll(dataToSave);
        log.info("Saved {} symbol price records to database", dataToSave.size());
        priceBuffer.clear();
      }
    } catch (Exception e) {
      log.error("Error saving priceBuffered data: ", e);
    }
  }

  public void addToBuffer(Price price) {
    priceBuffer.add(price);
  }

  public List<Price> getInitialPriceOfSymbolName(String symbolName) {
    List<Price> prices =
        priceRepository.findBySymbolOfPageable(symbolName, PageRequest.of(0, 3000));

    List<Price> restPrices = priceBuffer.getBufferedDataOfSymbol(symbolName);
    List<Price> allPrices = new ArrayList<>();

    allPrices.addAll(prices);
    allPrices.addAll(restPrices);

    return allPrices;
  }
}
