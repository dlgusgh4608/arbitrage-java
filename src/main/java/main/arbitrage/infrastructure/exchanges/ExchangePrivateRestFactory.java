package main.arbitrage.infrastructure.exchanges;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.domain.userEnv.entity.UserEnv;
import main.arbitrage.global.util.aes.AESCrypto;
import main.arbitrage.infrastructure.binance.BinanceClient;
import main.arbitrage.infrastructure.binance.BinancePrivateRestService;
import main.arbitrage.infrastructure.exchanges.dto.ExchangePrivateRestPair;
import main.arbitrage.infrastructure.upbit.UpbitClient;
import main.arbitrage.infrastructure.upbit.UpbitPrivateRestService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangePrivateRestFactory {
  private final AESCrypto aesCrypto;
  private final UpbitClient upbitClient;
  private final BinanceClient binanceClient;
  private final ObjectMapper objectMapper;
  private final SymbolVariableService symbolVariableService;

  public ExchangePrivateRestPair create(UserEnv userEnv) {
    List<String> symbolNames = symbolVariableService.getSupportedSymbolNames();

    UpbitPrivateRestService upbitService =
        new UpbitPrivateRestService(
            aesCrypto.decrypt(userEnv.getUpbitAccessKey()),
            aesCrypto.decrypt(userEnv.getUpbitSecretKey()),
            upbitClient,
            objectMapper,
            symbolNames);

    BinancePrivateRestService binanceService =
        new BinancePrivateRestService(
            aesCrypto.decrypt(userEnv.getBinanceAccessKey()),
            aesCrypto.decrypt(userEnv.getBinanceSecretKey()),
            binanceClient,
            objectMapper,
            symbolNames);

    return ExchangePrivateRestPair.builder().upbit(upbitService).binance(binanceService).build();
  }

  public ExchangePrivateRestPair create(
      String upbitAccessKey,
      String upbitSecretKey,
      String binanceAccessKey,
      String binanceSecretKey) {
    List<String> symbolNames = symbolVariableService.getSupportedSymbolNames();

    UpbitPrivateRestService upbitService =
        new UpbitPrivateRestService(
            upbitAccessKey, upbitSecretKey, upbitClient, objectMapper, symbolNames);

    BinancePrivateRestService binanceService =
        new BinancePrivateRestService(
            binanceAccessKey, binanceSecretKey, binanceClient, objectMapper, symbolNames);

    return ExchangePrivateRestPair.builder().upbit(upbitService).binance(binanceService).build();
  }
}
