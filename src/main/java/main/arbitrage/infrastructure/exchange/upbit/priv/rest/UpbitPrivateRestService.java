package main.arbitrage.infrastructure.exchange.upbit.priv.rest;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import main.arbitrage.infrastructure.exchange.ExchangePrivateRestService;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.account.UpbitGetAccountResponseDto;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order.UpbitGetOrderResponseDto;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order.UpbitOrderEnum;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order.UpbitOrderEnum.State;;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order.UpbitOrderEnum.OrdType;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.dto.order.UpbitPostOrderRequestDto;
import main.arbitrage.infrastructure.exchange.upbit.priv.rest.exception.UpbitPrivateRestException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpbitPrivateRestService implements ExchangePrivateRestService {
    private final String accessKey;
    private final String secretKey;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    private static final String SERVER_URI = "https://api.upbit.com";

    public UpbitPrivateRestService(String accessKey, String secretKey, OkHttpClient okHttpClient, ObjectMapper objectMapper) {
        if (accessKey.isEmpty() || secretKey.isEmpty()) {
            throw new WeakKeyException("The specified key byte array is 0 bits");
        }
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.okHttpClient = okHttpClient;
        this.objectMapper = objectMapper;
    }

    public List<UpbitGetAccountResponseDto> getAccount() throws UpbitPrivateRestException, IOException {
        String token = generateToken();
        Request request = new Request.Builder()
                .url(SERVER_URI + "/v1/accounts")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        Response response = okHttpClient.newCall(request).execute();

        String responseBody = response.body().string();

        if (!response.isSuccessful()) validateResponse(responseBody);

        return objectMapper.readValue(
                responseBody,
                objectMapper.getTypeFactory().constructCollectionType(List.class, UpbitGetAccountResponseDto.class)
        );
    }

    public Optional<UpbitGetAccountResponseDto> getKRW() throws UpbitPrivateRestException, IOException {
        List<UpbitGetAccountResponseDto> account = getAccount();
        return account.stream().filter(upbitAccount -> upbitAccount.getCurrency().equals("KRW")).findFirst();
    }

    public String order(String market, UpbitOrderEnum.Side side, UpbitOrderEnum.OrdType ordType, Double price, Double volume) throws UpbitPrivateRestException, IOException {
        if (market == null || side == null || ordType == null) {
            throw new UpbitPrivateRestException("(업비트) 잘못 된 주문 API 요청입니다.", "validation_error");
        }

        // orderType Validation
        switch (ordType) {
            case limit -> {
                if (volume == null || price == null) {
                    throw new UpbitPrivateRestException("(업비트) 잘못 된 주문 API 요청입니다.", "validation_error");
                }
            }
            // 매수
            case price -> {
                if (price == null || side.equals(UpbitOrderEnum.Side.ask)) {
                    throw new UpbitPrivateRestException("(업비트) 잘못 된 주문 API 요청입니다.", "validation_error");
                }
            }
            // 매도
            case market -> {
                if (volume == null || side.equals(UpbitOrderEnum.Side.bid)) {
                    throw new UpbitPrivateRestException("(업비트) 잘못 된 주문 API 요청입니다.", "validation_error");
                }
            }
        }

        UpbitPostOrderRequestDto requestDto = UpbitPostOrderRequestDto.builder()
                .market(market)
                .side(side)
                .ordType(ordType)
                .price(price)
                .volume(volume)
                .build();

        Map<String, Object> map = objectMapper.convertValue(requestDto, Map.class);

        String token = generateToken(map);

        RequestBody body = RequestBody.create(
            objectMapper.writeValueAsString(map).getBytes(StandardCharsets.UTF_8),
            MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(SERVER_URI + "/v1/orders")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        Response response = okHttpClient.newCall(request).execute();

        String responseBody = response.body().string();

        /*
         * responseBody에 있는 uuid를 통해 order를 조회해야합니다.
         * response값은 아직 거래가 체결되지 않은 상태로 오기때문입니다.
         * order get method를 state success가 나올때까지 2초 딜레이 3회 재귀 돌게하여 데이터 get
         * */

        if (!response.isSuccessful()) validateResponse(responseBody);

        JsonNode json = objectMapper.readTree(responseBody);

        return json.get("uuid").asText();
    }

    public UpbitGetOrderResponseDto order(String uuid, int repeat) throws UpbitPrivateRestException, InterruptedException, IOException {
        if(repeat < 2) return null;
        
        Map<String, Object> map = Map.of("uuid", uuid);

        String token = generateToken(map);

        Request request = new Request.Builder()
                .url(SERVER_URI + "/v1/order" + "?" + generateQueryString(map))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        Response response = okHttpClient.newCall(request).execute();
        String responseBody = response.body().string();

        if (!response.isSuccessful()) validateResponse(responseBody);

        UpbitGetOrderResponseDto dto = objectMapper.readValue(responseBody, UpbitGetOrderResponseDto.class);

        if (dto.getState().equals(State.wait) || dto.getState().equals(State.watch)) {
            Thread.sleep(1000);
            return order(uuid, repeat - 1);
        }

        return dto;
    }

    @Override
    public void validateResponse(String responseBody) throws UpbitPrivateRestException, JsonProcessingException {
        JsonNode json = objectMapper.readTree(responseBody);
        String errorCode = json.get("error").get("name").asText();
        switch (errorCode) {
            case "invalid_query_payload":
                throw new UpbitPrivateRestException("(업비트) JWT 헤더의 페이로드가 올바르지 않습니다.", errorCode);
            case "jwt_verification":
                throw new UpbitPrivateRestException("(업비트) JWT 헤더 검증에 실패했습니다.", errorCode);
            case "expired_access_key":
                throw new UpbitPrivateRestException("(업비트) API 키가 만료되었습니다.", errorCode);
            case "nonce_used":
                throw new UpbitPrivateRestException("(업비트) 이미 사용된 nonce값입니다.", errorCode);
            case "no_authorization_i_p":
                throw new UpbitPrivateRestException("(업비트) 허용되지 않은 IP 주소입니다.", errorCode);
            case "out_of_scope":
                throw new UpbitPrivateRestException("(업비트) 허용되지 않은 기능입니다.", errorCode);
            case "create_ask_error":
                throw new UpbitPrivateRestException("(업비트) 매수 주문 요청 정보가 올바르지 않습니다.", errorCode);
            case "create_bid_error":
                throw new UpbitPrivateRestException("(업비트) 매도 주문 요청 정보가 올바르지 않습니다.", errorCode);
            case "insufficient_funds_ask":
                throw new UpbitPrivateRestException("(업비트) 매수 가능 잔고가 부족합니다.", errorCode);
            case "insufficient_funds_bid":
                throw new UpbitPrivateRestException("(업비트) 매도 가능 잔고가 부족합니다.", errorCode);
            case "under_min_total_ask":
                throw new UpbitPrivateRestException("(업비트) 최소 매수 금액 미만입니다.", errorCode);
            case "under_min_total_bid":
                throw new UpbitPrivateRestException("(업비트) 최소 매도 금액 미만입니다.", errorCode);
            case "withdraw_address_not_registerd":
                throw new UpbitPrivateRestException("(업비트) 허용 되지 않은 출금 주소입니다.", errorCode);
            case "validation_error", "invalid_parameter":
                throw new UpbitPrivateRestException("(업비트) 잘못 된 주문 API 요청입니다.", errorCode);
            default:
                throw new UpbitPrivateRestException("(업비트) 알 수 없는 에러가 발생했습니다.", errorCode);
        }
    }

    @Override
    public String generateToken() {
        try {
            String base64EncodedKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
            byte[] keyBytes = Decoders.BASE64.decode(base64EncodedKey);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);

            return Jwts.builder()
                    .claim("access_key", accessKey)
                    .claim("nonce", UUID.randomUUID().toString())
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            throw new UpbitPrivateRestException("(업비트) JWT 헤더 검증에 실패했습니다.", "jwt_verification");
        }
    }

    @Override
    public String generateToken(Map<String, Object> params) {
        try {
            String queryString = generateQueryString(params);
            String queryHash = generateQueryHash(queryString);
            String base64EncodedKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
            byte[] keyBytes = Decoders.BASE64.decode(base64EncodedKey);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);

            return Jwts.builder()
                    .claim("access_key", accessKey)
                    .claim("nonce", UUID.randomUUID().toString())
                    .claim("query_hash", queryHash)
                    .claim("query_hash_alg", "SHA512")
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            throw new UpbitPrivateRestException("(업비트) JWT 헤더 검증에 실패했습니다.", "jwt_verification");
        }
    }

    @Override
    public String generateQueryString(Map<String, Object> params) {
        return params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    @Override
    public String generateQueryHash(String queryString) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes(StandardCharsets.UTF_8));
        return String.format("%0128x", new BigInteger(1, md.digest()));

    }
}