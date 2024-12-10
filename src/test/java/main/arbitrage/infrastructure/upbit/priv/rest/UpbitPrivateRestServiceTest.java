package main.arbitrage.infrastructure.upbit.priv.rest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.UpbitPrivateRestService;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.exception.UpbitPrivateRestException;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UpbitPrivateRestServiceTest {

    private UpbitPrivateRestService upbitPrivateRestService;
    private final String accessKey = "12345oiUshbzzapfhdEnfgdmftndlTsHFjfp7qf8";
    private final String secretKey = "xrh0y6789106kwKPeUvDwRfdzzfdzzQMKQwGReId";
    private final String shortKey = "hellworld";

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
        upbitPrivateRestService = new UpbitPrivateRestService(accessKey, secretKey, mockClient, objectMapper);
    }

    @Nested
    @DisplayName("API 에러 응답 테스트")
    class ApiErrorResponseTest {

        @Test
        @DisplayName("잘못된 API 키 형식 에러")
        void invalidQueryPayloadTest() throws Exception {
            // given
            String errorJson = createErrorJson("invalid_query_payload");
            mockErrorResponse(errorJson);

            // when & then
            assertThatThrownBy(() -> upbitPrivateRestService.getAccount())
                    .isInstanceOf(UpbitPrivateRestException.class)
                    .hasMessage("(업비트) JWT 헤더의 페이로드가 올바르지 않습니다.")
                    .hasFieldOrPropertyWithValue("errorCode", "invalid_query_payload");
        }

        @Test
        @DisplayName("시크릿키가 32byte를 안넘길때 짧은 키일때")
        void jwtVerificationOfShortKeyTest() {
            // given
            UpbitPrivateRestService shortKeyService = new UpbitPrivateRestService(accessKey, shortKey, mockClient, objectMapper);

            // when & then
            assertThatThrownBy(shortKeyService::getAccount)
                    .isInstanceOf(UpbitPrivateRestException.class)
                    .hasMessage("(업비트) JWT 헤더 검증에 실패했습니다.")
                    .hasFieldOrPropertyWithValue("errorCode", "jwt_verification");
        }

        @Test
        @DisplayName("JWT 검증 실패 에러")
        void jwtVerificationTest() throws Exception {
            // given
            String errorJson = createErrorJson("jwt_verification");
            mockErrorResponse(errorJson);

            // when & then
            assertThatThrownBy(() -> upbitPrivateRestService.getAccount())
                    .isInstanceOf(UpbitPrivateRestException.class)
                    .hasMessage("(업비트) JWT 헤더 검증에 실패했습니다.")
                    .hasFieldOrPropertyWithValue("errorCode", "jwt_verification");
        }

        @Test
        @DisplayName("만료된 API 키 에러")
        void expiredAccessKeyTest() throws Exception {
            // given
            String errorJson = createErrorJson("expired_access_key");
            mockErrorResponse(errorJson);

            // when & then
            assertThatThrownBy(() -> upbitPrivateRestService.getAccount())
                    .isInstanceOf(UpbitPrivateRestException.class)
                    .hasMessage("(업비트) API 키가 만료되었습니다.")
                    .hasFieldOrPropertyWithValue("errorCode", "expired_access_key");
        }

        @Test
        @DisplayName("허용되지 않은 IP 에러")
        void noAuthorizationIpTest() throws Exception {
            // given
            String errorJson = createErrorJson("no_authorization_i_p");
            mockErrorResponse(errorJson);

            // when & then
            assertThatThrownBy(() -> upbitPrivateRestService.getAccount())
                    .isInstanceOf(UpbitPrivateRestException.class)
                    .hasMessage("(업비트) 허용되지 않은 IP 주소입니다.")
                    .hasFieldOrPropertyWithValue("errorCode", "no_authorization_i_p");
        }

        private String createErrorJson(String errorName) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            ObjectNode error = objectMapper.createObjectNode();
            error.put("name", errorName);
            errorNode.set("error", error);
            return errorNode.toString();
        }

        private void mockErrorResponse(String errorJson) throws Exception {
            when(mockResponse.isSuccessful()).thenReturn(false);
            when(mockResponse.body()).thenReturn(mockResponseBody);
            when(mockResponseBody.string()).thenReturn(errorJson);
            when(mockClient.newCall(any())).thenReturn(mock());
            when(mockClient.newCall(any()).execute()).thenReturn(mockResponse);
        }
    }
}