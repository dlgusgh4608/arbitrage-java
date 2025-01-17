package main.arbitrage.infrastructure.exchange.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.domain.userEnv.entity.UserEnv;
import main.arbitrage.global.util.aes.AESCrypto;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
import main.arbitrage.infrastructure.exchange.dto.ExchangePrivateRestPair;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangePrivateRestFactory {
  private final AESCrypto aesCrypto;
  private final OkHttpClient okHttpClient;
  private final ObjectMapper objectMapper;
  private final SymbolVariableService symbolVariableService;

  public ExchangePrivateRestPair create(UserEnv userEnv) {
    List<String> symbolNames = symbolVariableService.getSupportedSymbolNames();

    UpbitPrivateRestService upbitService =
        new UpbitPrivateRestService(
            aesCrypto.decrypt(userEnv.getUpbitAccessKey()),
            aesCrypto.decrypt(userEnv.getUpbitSecretKey()),
            okHttpClient,
            objectMapper,
            symbolNames);

    BinancePrivateRestService binanceService =
        new BinancePrivateRestService(
            aesCrypto.decrypt(userEnv.getBinanceAccessKey()),
            aesCrypto.decrypt(userEnv.getBinanceSecretKey()),
            okHttpClient,
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
            upbitAccessKey, upbitSecretKey, okHttpClient, objectMapper, symbolNames);

    BinancePrivateRestService binanceService =
        new BinancePrivateRestService(
            binanceAccessKey, binanceSecretKey, okHttpClient, objectMapper, symbolNames);

    return ExchangePrivateRestPair.builder().upbit(upbitService).binance(binanceService).build();
  }
}
