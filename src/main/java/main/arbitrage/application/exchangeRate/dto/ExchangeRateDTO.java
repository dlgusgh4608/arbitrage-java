package main.arbitrage.application.exchangeRate.dto;

import lombok.Builder;
import lombok.Getter;
import main.arbitrage.domain.exchangeRate.entity.ExchangeRate;

@Getter
@Builder
public class ExchangeRateDTO {
    private String toCurrency;
    private String fromCurrency;
    private double rate;

    public static ExchangeRate toEntity(ExchangeRateDTO dto) {
        return ExchangeRate.builder().toCurrency(dto.getToCurrency())
                .fromCurrency(dto.getFromCurrency()).rate(dto.getRate()).build();
    }
}
