package main.arbitrage.presentation.dto.view;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import main.arbitrage.infrastructure.binance.dto.enums.BinanceEnums.MarginType;
import main.arbitrage.infrastructure.binance.dto.response.BinanceLeverageBracketResponse;
import main.arbitrage.infrastructure.binance.dto.response.BinancePositionInfoResponse;
import main.arbitrage.infrastructure.upbit.dto.response.UpbitAccountResponse;
import main.arbitrage.presentation.dto.response.OrderResponse;

@Builder
@Getter
public class UserTradeInfo {
  private final Double usdt;
  private final Double krw;
  private final MarginType marginType;
  private final Integer leverage;
  private final List<BinanceLeverageBracketResponse.Brackets> brackets;
  private final UpbitAccountResponse upbitPosition;
  private final BinancePositionInfoResponse binancePosition;
  private final List<OrderResponse> orders;

  public boolean isEmpty() {
    return usdt == null
        || krw == null
        || marginType == null
        || leverage == null
        || brackets == null;
  }
}
