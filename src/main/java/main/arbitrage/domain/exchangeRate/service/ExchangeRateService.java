package main.arbitrage.domain.exchangeRate.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.application.exchangeRate.dto.ExchangeRateDTO;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.exchangeRate.repository.ExchangeRateRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {
  private final ExchangeRateRepository exchangeRateRepository;
  private final Map<String, ExchangeRateDTO> prevExchangeRateMap = new ConcurrentHashMap<>();

  public ExchangeRate setExchangeRate(ExchangeRateDTO exchangeRateDto) {
    double rate = exchangeRateDto.getRate();

    if (rate == 0) return null;

    String key = exchangeRateDto.getFromCurrency() + "_" + exchangeRateDto.getToCurrency();

    ExchangeRateDTO prevDto = prevExchangeRateMap.get(key);

    if (prevDto == null) {
      log.info("New exchange rate [{}]: {}", key, rate);

      prevExchangeRateMap.put(key, exchangeRateDto);
    } else {
      double prevDtoRate = prevDto.getRate();
      if (prevDtoRate == rate) return null;

      log.info("Update exchange rate [{}]: {} -> {}", key, prevDtoRate, rate);

      prevExchangeRateMap.put(key, exchangeRateDto);
    }

    return exchangeRateRepository.save(ExchangeRateDTO.toEntity(exchangeRateDto));
  }

  public ExchangeRateDTO getExchangeRate(String fromCurrency, String toCurrency) {
    String key = fromCurrency.toUpperCase() + "_" + toCurrency.toUpperCase();
    return prevExchangeRateMap.get(key);
  }
}
