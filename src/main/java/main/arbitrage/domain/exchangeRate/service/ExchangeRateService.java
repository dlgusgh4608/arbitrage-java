package main.arbitrage.domain.exchangeRate.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.exchangeRate.exception.ExchangeRateErrorCode;
import main.arbitrage.domain.exchangeRate.exception.ExchangeRateException;
import main.arbitrage.domain.exchangeRate.repository.ExchangeRateRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {
  private final ExchangeRateRepository exchangeRateRepository;
  private final Map<String, ExchangeRate> prevExchangeRateMap = new ConcurrentHashMap<>();

  public ExchangeRate setExchangeRate(float rate, String from, String to) {
    try {
      String key = from + "_" + to;

      ExchangeRate prevExchangeRate = prevExchangeRateMap.get(key);
      ExchangeRate exchangeRate =
          ExchangeRate.builder().fromCurrency(from).toCurrency(to).rate(rate).build();

      if (prevExchangeRate == null) {
        log.info("New exchange rate [{}]: {}", key, rate);

        prevExchangeRateMap.put(key, exchangeRate);
      } else {
        float prevDtoRate = prevExchangeRate.getRate();
        if (prevDtoRate == rate) return null;

        log.info("Update exchange rate [{}]: {} -> {}", key, prevDtoRate, rate);

        prevExchangeRateMap.put(key, exchangeRate);
      }

      return exchangeRateRepository.save(exchangeRate);
    } catch (Exception e) {
      throw new ExchangeRateException(ExchangeRateErrorCode.UNKNOWN, e);
    }
  }

  private ExchangeRate getExchangeRate(String fromCurrency, String toCurrency, boolean nonNull) {
    try {
      String key = fromCurrency.toUpperCase() + "_" + toCurrency.toUpperCase();
      ExchangeRate exchangeRate = prevExchangeRateMap.get(key);
      if (nonNull && exchangeRate == null)
        throw new ExchangeRateException(
            ExchangeRateErrorCode.NOT_FOUND,
            String.format("Not found exchange rate\t from: %s, to: %s", fromCurrency, toCurrency));

      return exchangeRate;
    } catch (ExchangeRateException e) {
      throw e;
    } catch (Exception e) {
      throw new ExchangeRateException(ExchangeRateErrorCode.UNKNOWN, e);
    }
  }

  public ExchangeRate getNonNullUsdToKrw() {
    try {
      return getExchangeRate("USD", "KRW", true);
    } catch (ExchangeRateException e) {
      throw e;
    }
  }

  public ExchangeRate getUsdToKrw() {
    try {
      return getExchangeRate("USD", "KRW", false);
    } catch (ExchangeRateException e) {
      throw e;
    }
  }
}
