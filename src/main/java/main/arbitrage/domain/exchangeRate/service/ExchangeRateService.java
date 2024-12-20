package main.arbitrage.domain.exchangeRate.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.domain.exchangeRate.dto.ExchangeRateDto;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.exchangeRate.repository.ExchangeRateRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {
    private final ExchangeRateRepository exchangeRateRepository;
    private final Map<String, ExchangeRateDto> prevExchangeRateMap = new ConcurrentHashMap<>();

    public ExchangeRate setExchangeRate(ExchangeRateDto exchangeRateDto) {
        double rate = exchangeRateDto.getRate();

        if (rate == 0)
            return null;

        String key = exchangeRateDto.getFromCurrency() + "_" + exchangeRateDto.getToCurrency();

        ExchangeRateDto prevDto = prevExchangeRateMap.get(key);

        if (prevDto == null) {
            log.info("New exchange rate [{}]: {}", key, rate);

            prevExchangeRateMap.put(key, exchangeRateDto);
        } else {
            double prevDtoRate = prevDto.getRate();
            if (prevDtoRate == rate)
                return null;

            log.info("Update exchange rate [{}]: {} -> {}", key, prevDtoRate, rate);

            prevExchangeRateMap.put(key, exchangeRateDto);
        }

        return exchangeRateRepository.save(ExchangeRateDto.toEntity(exchangeRateDto));
    }

    public ExchangeRateDto getExchangeRate(String fromCurrency, String toCurrency) {
        String key = fromCurrency.toUpperCase() + "_" + toCurrency.toUpperCase();
        return prevExchangeRateMap.get(key);
    }
}
