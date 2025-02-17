package main.arbitrage.infrastructure.exchanges.dto;

import lombok.Builder;
import lombok.Getter;
import main.arbitrage.infrastructure.binance.BinancePrivateRestService;
import main.arbitrage.infrastructure.upbit.UpbitPrivateRestService;

@Getter
@Builder
public class ExchangePrivateRestPair {
  private final UpbitPrivateRestService upbit;
  private final BinancePrivateRestService binance;
}
