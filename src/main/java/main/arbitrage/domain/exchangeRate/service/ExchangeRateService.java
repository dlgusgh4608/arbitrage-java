package main.arbitrage.domain.exchangeRate.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.exchangeRate.dto.ExchangeRateDto;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;
import main.arbitrage.domain.exchangeRate.repository.ExchangeRateRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {
    private final ExchangeRateRepository exchangeRateRepository;
    private final Map<String, ExchangeRateDto> prevExchangeRateMap = new ConcurrentHashMap<>();

    public ExchangeRate setExchangeRate(ExchangeRateDto exchangeRateDto) {
        double rate = exchangeRateDto.getRate();
        if (rate == 0) return null;

        String key = exchangeRateDto.getFromCurrency() + "_" + exchangeRateDto.getToCurrency();

        ExchangeRateDto prevDto = prevExchangeRateMap.get(key);
        if (prevDto == null) return null;

        double prevDtoRate = prevDto.getRate();
        if (prevDtoRate == rate) return null;

        prevExchangeRateMap.put(key, exchangeRateDto);

        return exchangeRateRepository.save(ExchangeRateDto.toEntity(exchangeRateDto));
    }

    public ExchangeRateDto getExchangeRate(String fromCurrency, String toCurrency) {
        String key = fromCurrency.toUpperCase() + "_" + toCurrency.toUpperCase();
        return prevExchangeRateMap.get(key);
    }
}