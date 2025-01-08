package main.arbitrage.presentation.dto.view;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.MarginType;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceLeverageBracketResponse;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinancePositionInfoResponse;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitGetAccountResponse;
import main.arbitrage.presentation.dto.response.OrderResponse;

@Builder
@Getter
public class UserTradeInfo {
  private final Double usdt;
  private final Double krw;
  private final MarginType marginType;
  private final Integer leverage;
  private final List<BinanceLeverageBracketResponse.Brackets> brackets;
  private final UpbitGetAccountResponse upbitPosition;
  private final BinancePositionInfoResponse binancePosition;
  private final List<OrderResponse> orders;
}
