package main.arbitrage.infrastructure.binance.pub.rest;

import java.io.IOException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import main.arbitrage.infrastructure.exchange.binance.pub.rest.BinancePublicRestService;

@Disabled("real-test")
@SpringBootTest
class BinancePublicRestServiceRealTest {

    @Autowired
    private BinancePublicRestService binancePublicRestService;

    @Test
    @DisplayName("심볼 정보 얻기")
    // @Disabled("real-test")
    void getAccountTest() throws IOException {
        binancePublicRestService.getExchangeInfo();
    }

}
