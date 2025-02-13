package main.arbitrage.application.auto.dto;

import com.querydsl.core.annotations.QueryProjection;
import main.arbitrage.domain.tradingStrategy.entity.TradingStrategy;

public record AutomaticUserInfoDTO(
    // in UserEntity
    long userId,
    boolean autoFlag,

    // in UserEnvEntity
    String upbitAccessKey,
    String upbitSecretKey,
    String binanceAccessKey,
    String binanceSecretKey,

    // autoFlat가 false일 경우 null일 수 있음.
    TradingStrategy tradingStrategy) {

  @QueryProjection
  public AutomaticUserInfoDTO {
    // compact construct
  }
}
