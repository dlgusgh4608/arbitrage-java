// package main.arbitrage.infrastructure.binance.priv.rest;

// import java.io.IOException;
// import java.util.Arrays;
// import java.util.List;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Disabled;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Side;
// import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Type;
// import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceOrderResponse;
// import main.arbitrage.infrastructure.exchange.binance.exception.BinanceRestException;
// import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
// import okhttp3.OkHttpClient;

// class BinancePrivateRestServiceRealTest {
//     private BinancePrivateRestService binancePrivateRestService;
//     private final OkHttpClient okHttpClient = new OkHttpClient();
//     private final ObjectMapper objectMapper = new ObjectMapper();
//     private final List<String> testSymbol = Arrays.asList("BTC", "ETH");

//     @BeforeEach
//     void setUp() {
//         String accessKey = ""; // 실제 바이낸스 Access Key 입력
//         String secretKey = ""; // 실제 바이낸스 Secret Key 입력
//         binancePrivateRestService = new BinancePrivateRestService(accessKey, secretKey,
//                 okHttpClient, objectMapper, testSymbol);
//     }

//     @Test
//     @DisplayName("지갑 연결 테스트")
//     @Disabled("real-test")
//     void getAccountTest() throws BinanceRestException, IOException {
//         binancePrivateRestService.getAccount();
//     }

//     @Test
//     @DisplayName("실제 주문 테스트 (시장가)")
//     @Disabled("real-test")
//     void marketOrderTest() throws IOException {
//         String market = "ETHUSDT";
//         Side side = Side.SELL;
//         Type type = Type.MARKET;
//         Double volume = 0.012d;
//         Double price = null;

//         BinanceOrderResponse dto =
//                 binancePrivateRestService.order(market, side, type, volume, price);
//         System.out.println(dto);
//     }

//     @Test
//     @DisplayName("실제 주문 테스트 (지정가)")
//     @Disabled("real-test")
//     void limitOrderTest() throws IOException {
//         String market = "ETHUSDT";
//         Side side = Side.SELL;
//         Type type = Type.LIMIT;
//         Double volume = 0.012d;
//         Double price = 4015.15d;

//         binancePrivateRestService.order(market, side, type, volume, price);
//     }
// }
