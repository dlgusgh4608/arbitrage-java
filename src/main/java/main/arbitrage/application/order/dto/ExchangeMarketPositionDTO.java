package main.arbitrage.application.order.dto;

import lombok.Builder;
import lombok.Getter;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinancePositionInfoResponse;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitAccountResponse;

@Getter
@Builder
public class ExchangeMarketPositionDTO {
  private final Double krw;
  private final Double usdt;
  private final UpbitAccountResponse upbitPosition;
  private final BinancePositionInfoResponse binancePosition;
}
