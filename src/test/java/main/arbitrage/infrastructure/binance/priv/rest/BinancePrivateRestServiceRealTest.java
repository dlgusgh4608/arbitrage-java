package main.arbitrage.infrastructure.binance.priv.rest;

import com.fasterxml.jackson.databind.ObjectMapper;

import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.dto.order.BinanceOrderResponseDto;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.dto.order.BinanceOrderEnum.Side;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.dto.order.BinanceOrderEnum.Type;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.exception.BinancePrivateRestException;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.*;

import java.io.IOException;

class BinancePrivateRestServiceRealTest {
    private BinancePrivateRestService binancePrivateRestService;
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        String accessKey = "JzDZR923yBOO2xQ8Am0Pu5Ud0Jx45j7hmQmTt2iqrOSsyL7gPbvFjHj7YWi7Zv1O"; // 실제 바이낸스 Access Key 입력
        String secretKey = "lcRXiiNsZPOudIZHu5cknLYM7n2t48eBZLpCGJnij87usdSVg4Wi6ChXpP8TYbSV"; // 실제 바이낸스 Secret Key 입력
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

        BinanceOrderResponseDto dto = binancePrivateRestService.order(market, side, type, volume, price);
        System.out.println(dto);
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
