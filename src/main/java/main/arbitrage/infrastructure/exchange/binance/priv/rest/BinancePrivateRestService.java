package main.arbitrage.infrastructure.exchange.binance.priv.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.crypto.codec.Hex;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.security.WeakKeyException;
import main.arbitrage.infrastructure.exchange.ExchangePrivateRestService;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Side;
import main.arbitrage.infrastructure.exchange.binance.dto.enums.BinanceEnums.Type;
import main.arbitrage.infrastructure.exchange.binance.dto.request.BinancePostOrderRequest;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceGetAccountResponse;
import main.arbitrage.infrastructure.exchange.binance.dto.response.BinanceOrderResponse;
import main.arbitrage.infrastructure.exchange.binance.priv.rest.exception.BinancePrivateRestException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BinancePrivateRestService implements ExchangePrivateRestService {
    private final String accessKey;
    private final String secretKey;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final List<String> symbolNames;
    private static final String HASH_ALGORITHM = "HmacSHA256";

    private final String DEFAULT_URL = "https://fapi.binance.com/fapi";

    public BinancePrivateRestService(String accessKey, String secretKey, OkHttpClient okHttpClient,
            ObjectMapper objectMapper, List<String> symbolNames) {
        if (accessKey.isEmpty() || secretKey.isEmpty()) {
            throw new WeakKeyException("The specified key byte array is 0 bits");
        }
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.okHttpClient = okHttpClient;
        this.objectMapper = objectMapper;
        this.symbolNames = symbolNames;
    }

    public List<BinanceGetAccountResponse> getAccount()
            throws IOException, BinancePrivateRestException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("timestamp", System.currentTimeMillis());
        String queryString = generateToken(params);

        Request request = new Request.Builder().url(DEFAULT_URL + "/v3/balance" + "?" + queryString)
                .addHeader("Content-Type", "application/json").addHeader("X-MBX-APIKEY", accessKey)
                .get().build();

        Response response = okHttpClient.newCall(request).execute();

        String responseBody = response.body().string();

        if (!response.isSuccessful())
            validateResponse(responseBody);

        return objectMapper.readValue(responseBody, objectMapper.getTypeFactory()
                .constructCollectionType(List.class, BinanceGetAccountResponse.class));
    }

    public Optional<BinanceGetAccountResponse> getUSDT()
            throws IOException, BinancePrivateRestException {
        List<BinanceGetAccountResponse> account = getAccount();
        return account.stream().filter(binanceAccount -> binanceAccount.getAsset().equals("USDT"))
                .findFirst();
    }

    public BinanceOrderResponse order(String market, Side side, Type type, Double volume,
            Double price) throws IOException, BinancePrivateRestException {
        if (market == null || side == null || type == null || volume == null) {
            throw new BinancePrivateRestException("(바이낸스) 잘못 된 주문 API 요청입니다.", "validation_error");
        }

        if (type.equals(Type.LIMIT) && price == null) {
            throw new BinancePrivateRestException("(바이낸스) 잘못 된 주문 API 요청입니다.", "validation_error");
        }

        String symbol = convertSymbol(market);

        BinancePostOrderRequest requestDto =
                BinancePostOrderRequest.builder().newClientOrderId(UUID.randomUUID().toString())
                        .type(type).symbol(symbol).side(side).price(price).quantity(volume).build();


        Map<String, Object> map = objectMapper.convertValue(requestDto, LinkedHashMap.class);

        String queryString = generateToken(map);

        RequestBody body = RequestBody.create(new byte[0], null);

        String url = DEFAULT_URL + "/v1/order" + "?" + queryString;

        Request request =
                new Request.Builder().url(url).addHeader("Content-Type", "application/json")
                        .addHeader("X-MBX-APIKEY", accessKey).post(body).build();

        Response response = okHttpClient.newCall(request).execute();

        String responseBody = response.body().string();

        if (!response.isSuccessful())
            validateResponse(responseBody);

        return objectMapper.readValue(responseBody, BinanceOrderResponse.class);
    }

    @Override
    public String convertSymbol(String symbol) {
        String upperSymbol = symbol.toUpperCase().replace("USDT", "");

        if (!symbolNames.contains(upperSymbol))
            throw new BinancePrivateRestException("(바이낸스) 지원하지 않는 심볼입니다.", "BAD_SYMBOL");

        return upperSymbol + "USDT";
    }

    @Override
    public void validateResponse(String responseBody)
            throws BinancePrivateRestException, JsonProcessingException {
        JsonNode json = objectMapper.readTree(responseBody);
        String errorCode = json.get("code").asText();
        switch (errorCode) {
            // General Server or Network issues (10xx)
            case "-1000":
                throw new BinancePrivateRestException("(바이낸스) 알 수 없는 에러가 발생했습니다.", "UNKNOWN");
            case "-1001":
                throw new BinancePrivateRestException("(바이낸스) 내부 오류가 발생했습니다. 다시 시도해주세요.",
                        "DISCONNECTED");
            case "-1002":
                throw new BinancePrivateRestException("(바이낸스) 이 요청을 실행할 권한이 없습니다.", "UNAUTHORIZED");
            case "-1008":
                throw new BinancePrivateRestException("(바이낸스) 너무 많은 요청이 대기 중입니다.",
                        "TOO_MANY_REQUESTS");
            case "-1021":
                throw new BinancePrivateRestException("(바이낸스) 타임스탬프가 recvWindow를 벗어났습니다.",
                        "INVALID_TIMESTAMP");
            case "-1022":
                throw new BinancePrivateRestException("(바이낸스) 이 요청에 대한 서명이 유효하지 않습니다.",
                        "INVALID_SIGNATURE");

            // Request issues (11xx - 2xxx)
            case "-1102":
                throw new BinancePrivateRestException("(바이낸스) 필수 파라미터가 전송되지 않았거나, 비어있거나, 잘못되었습니다.",
                        "MANDATORY_PARAM_EMPTY_OR_MALFORMED");
            case "-1121":
                throw new BinancePrivateRestException("(바이낸스) 잘못된 심볼입니다.", "BAD_SYMBOL");
            case "-2010":
                throw new BinancePrivateRestException("(바이낸스) 새로운 주문이 거부되었습니다.",
                        "NEW_ORDER_REJECTED");
            case "-2013":
                throw new BinancePrivateRestException("(바이낸스) 주문이 존재하지 않습니다.", "NO_SUCH_ORDER");
            case "-2014":
                throw new BinancePrivateRestException("(바이낸스) 유효하지 않은 API 키 입니다.",
                        "BAD_API_KEY_FMT");
            case "-2015":
                throw new BinancePrivateRestException("(바이낸스) API 키, IP 또는 권한이 유효하지 않습니다.",
                        "INVALID_API_KEY_IP_PERMISSION");
            case "-2018":
                throw new BinancePrivateRestException("(바이낸스) 잔액이 부족합니다.",
                        "BALANCE_NOT_SUFFICIENT");

            // Filters and other issues (3xxx-5xxx)
            case "-4001":
                throw new BinancePrivateRestException("(바이낸스) 가격이 0보다 작습니다.",
                        "PRICE_LESS_THAN_ZERO");
            case "-4003":
                throw new BinancePrivateRestException("(바이낸스) 수량이 0보다 작습니다.", "QTY_LESS_THAN_ZERO");
            case "-4055":
                throw new BinancePrivateRestException("(바이낸스) 금액은 양수여야 합니다.",
                        "AMOUNT_MUST_BE_POSITIVE");
            default:
                throw new BinancePrivateRestException("(바이낸스) 알 수 없는 에러가 발생했습니다.", "UNKNOWN_ERROR");
        }
    }

    @Override
    public String generateToken() {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            String signature = generateQueryHash("");
            params.put("signature", signature);

            return generateQueryString(params);
        } catch (Exception e) {
            throw new BinancePrivateRestException("(바이낸스) 이 요청에 대한 서명이 유효하지 않습니다.",
                    "INVALID_SIGNATURE");
        }
    }

    @Override
    public String generateToken(Map<String, Object> params) {
        try {
            String queryString = generateQueryString(params);
            String signature = generateQueryHash(queryString);
            params.put("signature", signature);

            return generateQueryString(params);
        } catch (Exception e) {
            throw new BinancePrivateRestException("(바이낸스) 이 요청에 대한 서명이 유효하지 않습니다.",
                    "INVALID_SIGNATURE");
        }
    }

    @Override
    public String generateQueryString(Map<String, Object> params) {
        return params.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    @Override
    public String generateQueryHash(String queryString)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance(HASH_ALGORITHM);
        SecretKeySpec secretKeySpec =
                new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HASH_ALGORITHM);
        hmac.init(secretKeySpec);

        byte[] signatureBytes = hmac.doFinal(queryString.getBytes(StandardCharsets.UTF_8));
        return String.valueOf(Hex.encode(signatureBytes));
    }
}
