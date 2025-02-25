package main.arbitrage.domain.price.service;

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
import main.arbitrage.presentation.dto.response.ChartDataResponse;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceService {
  private final PriceBuffer priceBuffer;
  private final PriceRepository priceRepository;

  public void saveToPG() {
    try {
      if (priceBuffer.isEmpty()) return;

      List<Price> dataToSave = priceBuffer.getBufferedData();
      priceRepository.bulkInsert(dataToSave);
      log.info("Saved {} symbol price records to database", dataToSave.size());
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

  public AutoTradingStandardValueDTO getAutoTradingValue(Symbol symbol, int minutes) {
    return priceRepository.getAutoTradingStandardValue(symbol, minutes);
  }

  public List<ChartDataResponse> getPremiumOHLC(Symbol symbol, int intervalMinutes, long lastTime) {
    return priceRepository.getPremiumOHLC(symbol, intervalMinutes, lastTime);
  }
}
