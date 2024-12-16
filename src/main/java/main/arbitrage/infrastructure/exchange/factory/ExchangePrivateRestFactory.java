package main.arbitrage.infrastructure.exchange.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.userEnv.entity.UserEnv;
import main.arbitrage.global.util.aes.AESCrypto;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
import main.arbitrage.infrastructure.exchange.factory.dto.ExchangePrivateRestPair;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangePrivateRestFactory {
    private final AESCrypto aesCrypto;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    public ExchangePrivateRestPair create(UserEnv userEnv) throws Exception {
        UpbitPrivateRestService upbitService = new UpbitPrivateRestService(
                aesCrypto.decrypt(userEnv.getUpbitAccessKey()),
                aesCrypto.decrypt(userEnv.getUpbitSecretKey()),
                okHttpClient,
                objectMapper
        );

        BinancePrivateRestService binanceService = new BinancePrivateRestService(
                aesCrypto.decrypt(userEnv.getBinanceAccessKey()),
                aesCrypto.decrypt(userEnv.getBinanceSecretKey()),
                okHttpClient,
                objectMapper
        );

        return new ExchangePrivateRestPair(upbitService, binanceService);
    }
}