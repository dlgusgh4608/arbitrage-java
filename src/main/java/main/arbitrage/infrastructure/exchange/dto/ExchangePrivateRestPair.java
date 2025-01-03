package main.arbitrage.infrastructure.exchange.dto;

import lombok.Builder;
import lombok.Getter;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;

@Getter
@Builder
public class ExchangePrivateRestPair {
  private final UpbitPrivateRestService upbit;
  private final BinancePrivateRestService binance;
}
