package main.arbitrage.infrastructure.exchange.upbit.priv.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import main.arbitrage.infrastructure.exchange.ExchangeRestService;
import main.arbitrage.infrastructure.exchange.binance.exception.BinanceRestException;
import main.arbitrage.infrastructure.exchange.upbit.exception.UpbitRestException;
import okhttp3.OkHttpClient;

public abstract class BaseUpbitPrivateRestService implements ExchangeRestService {
  protected final String accessKey;
  protected final String secretKey;
  protected final OkHttpClient okHttpClient;
  protected final ObjectMapper objectMapper;
  protected final List<String> symbolNames;

  protected static final String SERVER_URI = "https://api.upbit.com";

  public BaseUpbitPrivateRestService(
      String accessKey,
      String secretKey,
      OkHttpClient okHttpClient,
      ObjectMapper objectMapper,
      List<String> symbolNames) {
    if (accessKey.isEmpty() || secretKey.isEmpty()) {
      throw new WeakKeyException("The specified key byte array is 0 bits");
    }
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.okHttpClient = okHttpClient;
    this.objectMapper = objectMapper;
    this.symbolNames = symbolNames;
  }

  @Override
  public String convertSymbol(String symbol) {
    String upperSymbol = symbol.toUpperCase().replace("KRW-", "");

    if (!symbolNames.contains(upperSymbol))
      throw new BinanceRestException("(업비트) 지원하지 않는 심볼입니다.", "invalid_symbol");

    return "KRW-" + upperSymbol;
  }

  @Override
  public String generateQueryHash(String queryString) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-512");
    md.update(queryString.getBytes(StandardCharsets.UTF_8));
    return String.format("%0128x", new BigInteger(1, md.digest()));
  }

  @Override
  public String generateQueryString(Map<String, Object> params) {
    return params.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining("&"));
  }

  @Override
  public String generateToken() {
    try {
      String base64EncodedKey =
          Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
      byte[] keyBytes = Decoders.BASE64.decode(base64EncodedKey);
      SecretKey key = Keys.hmacShaKeyFor(keyBytes);

      return Jwts.builder()
          .claim("access_key", accessKey)
          .claim("nonce", UUID.randomUUID().toString())
          .signWith(key)
          .compact();
    } catch (Exception e) {
      throw new UpbitRestException("(업비트) JWT 헤더 검증에 실패했습니다.", "jwt_verification");
    }
  }

  @Override
  public String generateToken(Map<String, Object> params) {
    try {
      String queryString = generateQueryString(params);
      String queryHash = generateQueryHash(queryString);
      String base64EncodedKey =
          Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
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
      throw new UpbitRestException("(업비트) JWT 헤더 검증에 실패했습니다.", "jwt_verification");
    }
  }

  @Override
  public void validateResponse(String responseBody)
      throws UpbitRestException, JsonProcessingException {
    JsonNode json = objectMapper.readTree(responseBody);
    String errorCode = json.get("error").get("name").asText();
    switch (errorCode) {
      case "invalid_query_payload":
        throw new UpbitRestException("(업비트) JWT 헤더의 페이로드가 올바르지 않습니다.", errorCode);
      case "jwt_verification":
        throw new UpbitRestException("(업비트) JWT 헤더 검증에 실패했습니다.", errorCode);
      case "expired_access_key":
        throw new UpbitRestException("(업비트) API 키가 만료되었습니다.", errorCode);
      case "nonce_used":
        throw new UpbitRestException("(업비트) 이미 사용된 nonce값입니다.", errorCode);
      case "no_authorization_i_p":
        throw new UpbitRestException("(업비트) 허용되지 않은 IP 주소입니다.", errorCode);
      case "out_of_scope":
        throw new UpbitRestException("(업비트) 허용되지 않은 기능입니다.", errorCode);
      case "create_ask_error":
        throw new UpbitRestException("(업비트) 매수 주문 요청 정보가 올바르지 않습니다.", errorCode);
      case "create_bid_error":
        throw new UpbitRestException("(업비트) 매도 주문 요청 정보가 올바르지 않습니다.", errorCode);
      case "insufficient_funds_ask":
        throw new UpbitRestException("(업비트) 매수 가능 잔고가 부족합니다.", errorCode);
      case "insufficient_funds_bid":
        throw new UpbitRestException("(업비트) 매도 가능 잔고가 부족합니다.", errorCode);
      case "under_min_total_ask":
        throw new UpbitRestException("(업비트) 최소 매수 금액 미만입니다.", errorCode);
      case "under_min_total_bid":
        throw new UpbitRestException("(업비트) 최소 매도 금액 미만입니다.", errorCode);
      case "withdraw_address_not_registerd":
        throw new UpbitRestException("(업비트) 허용 되지 않은 출금 주소입니다.", errorCode);
      case "validation_error", "invalid_parameter":
        throw new UpbitRestException("(업비트) 잘못 된 주문 API 요청입니다.", errorCode);
      default:
        throw new UpbitRestException("(업비트) 알 수 없는 에러가 발생했습니다.", errorCode);
    }
  }
}
