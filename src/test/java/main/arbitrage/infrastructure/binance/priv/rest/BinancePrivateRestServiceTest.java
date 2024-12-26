package main.arbitrage.infrastructure.binance.priv.rest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.arbitrage.infrastructure.exchange.binance.exception.BinanceRestException;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.BinancePrivateRestService;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

class BinancePrivateRestServiceTest {

    private BinancePrivateRestService binancePrivateRestService;
    private final String accessKey = "testAccessKey";
    private final String secretKey = "testSecretKey";
    private final List<String> testSymbol = Arrays.asList("BTC", "ETH");

    @Mock
    private OkHttpClient mockClient;

    @Mock
    private Response mockResponse;

    @Mock
    private ResponseBody mockResponseBody;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        binancePrivateRestService = new BinancePrivateRestService(accessKey, secretKey, mockClient,
                objectMapper, testSymbol);
    }

    @Nested
    @DisplayName("API 에러 응답 테스트")
    class ApiErrorResponseTest {

        @Test
        @DisplayName("잘못된 API 키 에러")
        void invalidApiKeyTest() throws Exception {
            mockErrorResponse(-2014, "API-key format invalid.");

            assertThatThrownBy(() -> binancePrivateRestService.getAccount())
                    .isInstanceOf(BinanceRestException.class)
                    .hasMessage("(바이낸스) 유효하지 않은 API 키 입니다.")
                    .hasFieldOrPropertyWithValue("errorCode", "BAD_API_KEY_FMT");
        }

        @Test
        @DisplayName("서명 실패 에러")
        void signatureFailureTest() throws Exception {
            mockErrorResponse(-1022, "Signature for this request is not valid.");

            assertThatThrownBy(() -> binancePrivateRestService.getAccount())
                    .isInstanceOf(BinanceRestException.class)
                    .hasMessage("(바이낸스) 이 요청에 대한 서명이 유효하지 않습니다.")
                    .hasFieldOrPropertyWithValue("errorCode", "INVALID_SIGNATURE");
        }

        @Test
        @DisplayName("타임스탬프 에러")
        void timestampErrorTest() throws Exception {
            mockErrorResponse(-1021,
                    "Timestamp for this request was 1000ms ahead of the server's time.");

            assertThatThrownBy(() -> binancePrivateRestService.getAccount())
                    .isInstanceOf(BinanceRestException.class)
                    .hasMessage("(바이낸스) 타임스탬프가 recvWindow를 벗어났습니다.")
                    .hasFieldOrPropertyWithValue("errorCode", "INVALID_TIMESTAMP");
        }

        @Test
        @DisplayName("IP 제한 에러")
        void ipRestrictionTest() throws Exception {
            mockErrorResponse(-2015, "Invalid API-key, IP, or permissions for action.");

            assertThatThrownBy(() -> binancePrivateRestService.getAccount())
                    .isInstanceOf(BinanceRestException.class)
                    .hasMessage("(바이낸스) API 키, IP 또는 권한이 유효하지 않습니다.")
                    .hasFieldOrPropertyWithValue("errorCode", "INVALID_API_KEY_IP_PERMISSION");
        }

        private String createErrorJson(int code, String message) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("code", code);
            errorNode.put("msg", message);
            return errorNode.toString();
        }

        private void mockErrorResponse(int code, String message) throws Exception {
            String errorJson = createErrorJson(code, message);
            when(mockResponse.isSuccessful()).thenReturn(false);
            when(mockResponse.body()).thenReturn(mockResponseBody);
            when(mockResponseBody.string()).thenReturn(errorJson);
            when(mockClient.newCall(any())).thenReturn(mock());
            when(mockClient.newCall(any()).execute()).thenReturn(mockResponse);
        }
    }
}
