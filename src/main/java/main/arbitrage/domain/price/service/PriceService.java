package main.arbitrage.domain.price.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.application.auto.dto.AutoTradingStandardValueDTO;
import main.arbitrage.domain.price.buffer.PriceBuffer;
import main.arbitrage.domain.price.entity.Price;
import main.arbitrage.domain.price.exception.PriceErrorCode;
import main.arbitrage.domain.price.exception.PriceException;
import main.arbitrage.domain.price.repository.PriceRepository;
import main.arbitrage.domain.symbol.entity.Symbol;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceService {
  private final PriceBuffer priceBuffer;
  private final PriceRepository priceRepository;
  private final Runtime runtime = Runtime.getRuntime();

  public void saveToPG() {
    try {
      if (priceBuffer.isEmpty()) return;

      List<Price> dataToSave = priceBuffer.getBufferedData();
      priceRepository.bulkInsert(dataToSave);
      log.info("Saved {} symbol price records to database", dataToSave.size());

      final long mb = 1024 * 1024;

      log.info("========================== Memory Info ==========================");
      log.info("Free memory: {} MB", runtime.freeMemory() / mb);
      log.info("Allocated memory: {} MB", runtime.totalMemory() / mb);
      log.info("Max memory: {} MB", runtime.maxMemory() / mb);
      log.info(
          "Total free memory: {} MB",
          (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / mb);
      log.info("=================================================================");

      priceBuffer.clear();
    } catch (Exception e) {
      throw new PriceException(PriceErrorCode.UNKNOWN, e);
    }
  }

  public void addToBuffer(Price price) {
    try {
      if (price == null) {
        throw new PriceException(PriceErrorCode.PRICE_NULL, "Price가 null입니다.");
      }

      priceBuffer.add(price);
    } catch (PriceException e) {
      throw e;
    } catch (Exception e) {
      String errorMessage =
          String.format(
              "Price 버퍼 추가 실패 - 심볼: %s, 프리미엄: %s, 바이낸스: %s, 업비트: %s",
              price.getSymbol().getName(),
              price.getPremium(),
              price.getBinance(),
              price.getUpbit());
      throw new PriceException(PriceErrorCode.UNKNOWN, errorMessage, e);
    }
  }

  public List<Price> getInitialPriceOfSymbolName(String symbolName) {
    try {
      List<Price> prices = priceRepository.findBySymbolName(symbolName, PageRequest.of(0, 3000));

      List<Price> restPrices = priceBuffer.getBufferedDataOfSymbol(symbolName);
      List<Price> allPrices = new ArrayList<>();

      allPrices.addAll(prices);
      allPrices.addAll(restPrices);

      return allPrices;
    } catch (Exception e) {
      throw new PriceException(PriceErrorCode.UNKNOWN, e);
    }
  }

  public AutoTradingStandardValueDTO getAutoTradingValue(Symbol symbol, int minutes) {
    return priceRepository.getAutoTradingStandardValue(symbol, minutes);
  }
}
