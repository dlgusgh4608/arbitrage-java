package main.arbitrage.infrastructure.upbit.priv.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order.OrdType;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order.Side;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order.UpbitGetOrderResponseDto;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class UpbitPrivateRestServiceRealTest {
    private UpbitPrivateRestService upbitPrivateRestService;
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        String accessKey = "";  // 실제 업비트 Access Key 입력
        String secretKey = "";  // 실제 업비트 Secret Key 입력
        upbitPrivateRestService = new UpbitPrivateRestService(accessKey, secretKey, okHttpClient, objectMapper);
    }

    @Test
    @DisplayName("주문 테스트")
    @Order(1)
//    @Disabled("real-test")
    void orderTest() throws IOException {
        // given
        String market = "KRW-BTC";
        Side side = Side.bid;  // 매수
        OrdType ordType = OrdType.price;
        Double price = 5000d;  // 5천원

        // when
        String uuid = upbitPrivateRestService.order(market, side, ordType, price, null);

        // then
        assertNotNull(uuid);
        System.out.println("Order UUID: " + uuid);
    }

    @Test
    @DisplayName("주문 가져오기 테스트")
    @Order(2)
    @Disabled("real-test")
    void orderBuyAndGet() throws IOException, InterruptedException {
        // given
        String market = "KRW-BTC";
        Side side = Side.bid;  // 매수
        OrdType ordType = OrdType.price;
        Double price = 5000d;  // 5천원

        // when
        String uuid = upbitPrivateRestService.order(market, side, ordType, price, null);
        UpbitGetOrderResponseDto dto = upbitPrivateRestService.order(uuid, 5);

        // then
        System.out.println("getState: " + dto.getState());
        System.out.println("getExecutedVolume: " + dto.getExecutedVolume());
        System.out.println("getPaidFee: " + dto.getPaidFee());
    }
}
