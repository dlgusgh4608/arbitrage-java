package main.arbitrage.infrastructure.exchange.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.symbol.service.SymbolVariableService;
import main.arbitrage.domain.userEnv.entity.UserEnv;
import main.arbitrage.global.util.aes.AESCrypto;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
import main.arbitrage.infrastructure.exchange.factory.dto.ExchangePrivateRestPair;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;
import okhttp3.OkHttpClient;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangePrivateRestFactory {
    private final AESCrypto aesCrypto;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final SymbolVariableService symbolVariableService;

    public ExchangePrivateRestPair create(UserEnv userEnv) throws Exception {
        List<String> symbolNames = symbolVariableService.getSupportedSymbolNames();
        
        UpbitPrivateRestService upbitService = new UpbitPrivateRestService(
                aesCrypto.decrypt(userEnv.getUpbitAccessKey()),
                aesCrypto.decrypt(userEnv.getUpbitSecretKey()),
                okHttpClient,
                objectMapper,
                symbolNames
        );

        BinancePrivateRestService binanceService = new BinancePrivateRestService(
                aesCrypto.decrypt(userEnv.getBinanceAccessKey()),
                aesCrypto.decrypt(userEnv.getBinanceSecretKey()),
                okHttpClient,
                objectMapper,
                symbolNames
        );

        return new ExchangePrivateRestPair(upbitService, binanceService);
    }

    public ExchangePrivateRestPair create(String upbitAccessKey, String upbitSecretKey, String binanceAccessKey, String binanceSecretKey) throws Exception {
        List<String> symbolNames = symbolVariableService.getSupportedSymbolNames();

        UpbitPrivateRestService upbitService = new UpbitPrivateRestService(
                aesCrypto.decrypt(upbitAccessKey),
                aesCrypto.decrypt(upbitSecretKey),
                okHttpClient,
                objectMapper,
                symbolNames);

        BinancePrivateRestService binanceService = new BinancePrivateRestService(
                aesCrypto.decrypt(binanceAccessKey),
                aesCrypto.decrypt(binanceSecretKey),
                okHttpClient,
                objectMapper,
                symbolNames);

        return new ExchangePrivateRestPair(upbitService, binanceService);
    }
}