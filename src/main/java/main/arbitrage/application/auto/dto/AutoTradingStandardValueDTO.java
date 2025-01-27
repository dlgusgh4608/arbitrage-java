package main.arbitrage.application.auto.dto;

import com.querydsl.core.annotations.QueryProjection;

public record AutoTradingStandardValueDTO(
    float avgExchangeRate, float maxPremium, float minPremium) {
  @QueryProjection
  public AutoTradingStandardValueDTO {
    // compact construct
  }
}
