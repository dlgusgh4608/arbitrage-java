package main.arbitrage.infrastructure.exchange.upbit.priv.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.arbitrage.infrastructure.exchange.upbit.dto.enums.UpbitOrderEnums.OrdType;
import main.arbitrage.infrastructure.exchange.upbit.dto.enums.UpbitOrderEnums.Side;
import main.arbitrage.infrastructure.exchange.upbit.dto.enums.UpbitOrderEnums.State;
import main.arbitrage.infrastructure.exchange.upbit.dto.request.UpbitPostOrderRequest;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitGetAccountResponse;
import main.arbitrage.infrastructure.exchange.upbit.dto.response.UpbitGetOrderResponse;
import main.arbitrage.infrastructure.exchange.upbit.exception.UpbitRestException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpbitPrivateRestService extends BaseUpbitPrivateRestService {
    public UpbitPrivateRestService(String accessKey, String secretKey, OkHttpClient okHttpClient,
            ObjectMapper objectMapper, List<String> symbolNames) {
        super(accessKey, secretKey, okHttpClient, objectMapper, symbolNames);
    }

    public List<UpbitGetAccountResponse> getAccount() throws UpbitRestException, IOException {
        String token = generateToken();
        Request request = new Request.Builder().url(SERVER_URI + "/v1/accounts")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token).get().build();

        Response response = okHttpClient.newCall(request).execute();

        String responseBody = response.body().string();

        if (!response.isSuccessful())
            validateResponse(responseBody);

        return objectMapper.readValue(responseBody, objectMapper.getTypeFactory()
                .constructCollectionType(List.class, UpbitGetAccountResponse.class));
    }

    public Optional<UpbitGetAccountResponse> getKRW() throws UpbitRestException, IOException {
        List<UpbitGetAccountResponse> account = getAccount();
        return account.stream().filter(upbitAccount -> upbitAccount.currency().equals("KRW"))
                .findFirst();
    }

    public String order(String market, Side side, OrdType ordType, Double price, Double volume)
            throws UpbitRestException, IOException {
        if (market == null || side == null || ordType == null) {
            throw new UpbitRestException("(업비트) 잘못 된 주문 API 요청입니다.", "validation_error");
        }

        // orderType Validation
        switch (ordType) {
            case limit -> {
                if (volume == null || price == null) {
                    throw new UpbitRestException("(업비트) 잘못 된 주문 API 요청입니다.", "validation_error");
                }
            }
            // 매수
            case price -> {
                if (price == null || side.equals(Side.ask)) {
                    throw new UpbitRestException("(업비트) 잘못 된 주문 API 요청입니다.", "validation_error");
                }
            }
            // 매도
            case market -> {
                if (volume == null || side.equals(Side.bid)) {
                    throw new UpbitRestException("(업비트) 잘못 된 주문 API 요청입니다.", "validation_error");
                }
            }
        }

        String symbol = convertSymbol(market);

        UpbitPostOrderRequest requestDto = UpbitPostOrderRequest.builder().market(symbol).side(side)
                .ordType(ordType).price(price).volume(volume).build();

        Map<String, Object> map = objectMapper.convertValue(requestDto, Map.class);

        String token = generateToken(map);

        RequestBody body = RequestBody.create(
                objectMapper.writeValueAsString(map).getBytes(StandardCharsets.UTF_8),
                MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder().url(SERVER_URI + "/v1/orders")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token).post(body).build();

        Response response = okHttpClient.newCall(request).execute();

        String responseBody = response.body().string();

        /*
         * responseBody에 있는 uuid를 통해 order를 조회해야합니다. response값은 아직 거래가 체결되지 않은 상태로 오기때문입니다. order
         * get method를 state success가 나올때까지 2초 딜레이 3회 재귀 돌게하여 데이터 get
         */

        if (!response.isSuccessful())
            validateResponse(responseBody);

        JsonNode json = objectMapper.readTree(responseBody);

        return json.get("uuid").asText();
    }

    public UpbitGetOrderResponse order(String uuid, int repeat)
            throws UpbitRestException, InterruptedException, IOException {
        if (repeat < 2)
            return null;

        Map<String, Object> map = Map.of("uuid", uuid);

        String token = generateToken(map);

        Request request =
                new Request.Builder().url(SERVER_URI + "/v1/order" + "?" + generateQueryString(map))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "Bearer " + token).get().build();

        Response response = okHttpClient.newCall(request).execute();
        String responseBody = response.body().string();

        if (!response.isSuccessful())
            validateResponse(responseBody);

        UpbitGetOrderResponse dto =
                objectMapper.readValue(responseBody, UpbitGetOrderResponse.class);

        if (dto.state().equals(State.wait) || dto.state().equals(State.watch)) {
            Thread.sleep(1000);
            return order(uuid, repeat - 1);
        }

        return dto;
    }
}
