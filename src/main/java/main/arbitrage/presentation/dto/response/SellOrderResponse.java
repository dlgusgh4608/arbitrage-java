package main.arbitrage.presentation.dto.response;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import main.arbitrage.infrastructure.binance.dto.response.BinancePositionInfoResponse;
import main.arbitrage.infrastructure.upbit.dto.response.UpbitAccountResponse;

@Builder
@Getter
public class SellOrderResponse {
  private final Map<Long, OrderResponse> orderResponse;
  private final UpbitAccountResponse upbitPosition;
  private final BinancePositionInfoResponse binancePosition;
  private final double usdt;
  private final double krw;
}
