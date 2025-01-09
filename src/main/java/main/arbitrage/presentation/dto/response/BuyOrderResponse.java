package main.arbitrage.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinancePositionInfoResponse;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitAccountResponse;

@Builder
@Getter
public class BuyOrderResponse {
  private final OrderResponse orderResponse;
  private final UpbitAccountResponse upbitPosition;
  private final BinancePositionInfoResponse binancePosition;
  private final double usdt;
  private final double krw;
}
