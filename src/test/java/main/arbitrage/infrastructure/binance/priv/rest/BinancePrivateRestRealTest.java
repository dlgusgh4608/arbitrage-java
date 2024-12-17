package main.arbitrage.infrastructure.binance.priv.rest;

import com.fasterxml.jackson.databind.ObjectMapper;

import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.dto.order.*;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.exception.BinancePrivateRestException;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.*;

import java.io.IOException;

class BinancePrivateRestRealTest {
    private BinancePrivateRestService binancePrivateRestService;
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        String accessKey = ""; // 실제 바이낸스 Access Key 입력
        String secretKey = ""; // 실제 바이낸스 Secret Key 입력
        binancePrivateRestService = new BinancePrivateRestService(accessKey, secretKey, okHttpClient, objectMapper);
    }

    @Test
    @DisplayName("지갑 연결 테스트")
    @Disabled("real-test")
    void getAccountTest() throws BinancePrivateRestException, IOException {
        binancePrivateRestService.getAccount();
    }
    
    @Test
    @DisplayName("실제 주문 테스트 (시장가)")
    @Disabled("real-test")
    void marketOrderTest() throws IOException {
        String market = "ETHUSDT";
        Side side = Side.SELL;
        Type type = Type.MARKET;
        Double volume = 0.012d;
        Double price = null;

        binancePrivateRestService.order(market, side, type, volume, price);
        System.out.println("success");
    }

    @Test
    @DisplayName("실제 주문 테스트 (지정가)")
    @Disabled("real-test")
    void limitOrderTest() throws IOException {
        String market = "ETHUSDT";
        Side side = Side.SELL;
        Type type = Type.LIMIT;
        Double volume = 0.012d;
        Double price = 4015.15d;

        binancePrivateRestService.order(market, side, type, volume, price);
    }
}
