package main.arbitrage.infrastructure.upbit.priv.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order.UpbitOrderEnum.OrdType;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order.UpbitOrderEnum.Side;
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
    @Disabled("real-test")
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
    @DisplayName("구매 및 확인 테스트")
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
        System.out.println(dto);
    }

    @Test
    @DisplayName("판매 및 확인 테스트")
    @Disabled("real-test")
    void orderSellAndGet() throws IOException, InterruptedException {
        // given
        String market = "KRW-BTC";
        Side side = Side.ask; // 매도
        OrdType ordType = OrdType.market;
        Double price = null;
        Double volume = 0.00013197d;

        // when
        String uuid = upbitPrivateRestService.order(market, side, ordType, price, volume);
        UpbitGetOrderResponseDto dto = upbitPrivateRestService.order(uuid, 5);

        // then
        System.out.println(dto);
    }
}
