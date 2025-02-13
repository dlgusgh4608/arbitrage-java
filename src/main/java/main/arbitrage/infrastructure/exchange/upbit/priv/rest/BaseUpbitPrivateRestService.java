package main.arbitrage.infrastructure.exchange.upbit.priv.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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
import main.arbitrage.infrastructure.exchange.upbit.exception.UpbitErrorCode;
import main.arbitrage.infrastructure.exchange.upbit.exception.UpbitException;
import okhttp3.OkHttpClient;

public class BaseUpbitPrivateRestService implements ExchangeRestService {
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
    if (accessKey.isEmpty() || secretKey.isEmpty())
      throw new UpbitException(UpbitErrorCode.EMPTY_KEYS);
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.okHttpClient = okHttpClient;
    this.objectMapper = objectMapper;
    this.symbolNames = symbolNames;
  }

  @Override
  public String convertSymbol(String symbol) {
    String upperSymbol = symbol.toUpperCase().replace("KRW-", "");

    if (!symbolNames.contains(upperSymbol)) throw new UpbitException(UpbitErrorCode.INVALID_SYMBOL);

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
      throw new UpbitException(UpbitErrorCode.JWT_VERIFICATION, e);
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
      throw new UpbitException(UpbitErrorCode.JWT_VERIFICATION, e);
    }
  }

  @Override
  public void validateResponse(JsonNode json, String serverMsg) {
    String errorCode = json.get("error").get("name").asText();

    switch (errorCode) {
      case "invalid_query_payload":
        throw new UpbitException(
            UpbitErrorCode.INVALID_QUERY_PAYLOAD,
            String.format("errorCode: %s, msg: %s", errorCode, serverMsg));
      case "jwt_verification":
        throw new UpbitException(
            UpbitErrorCode.JWT_VERIFICATION,
            String.format("errorCode: %s, msg: %s", errorCode, serverMsg));
      case "expired_access_key":
        throw new UpbitException(
            UpbitErrorCode.EXPIRED_ACCESS_KEY,
            String.format("errorCode: %s, msg: %s", errorCode, serverMsg));
      case "nonce_used":
        throw new UpbitException(
            UpbitErrorCode.NONCE_USED,
            String.format("errorCode: %s, msg: %s", errorCode, serverMsg));
      case "no_authorization_ip":
        throw new UpbitException(
            UpbitErrorCode.NO_AUTHORIZATION_IP,
            String.format("errorCode: %s, msg: %s", errorCode, serverMsg));
      case "out_of_scope":
        throw new UpbitException(
            UpbitErrorCode.OUT_OF_SCOPE,
            String.format("errorCode: %s, msg: %s", errorCode, serverMsg));
      case "create_ask_error":
        throw new UpbitException(
            UpbitErrorCode.CREATE_ASK_ERROR,
            String.format("errorCode: %s, msg: %s", errorCode, serverMsg));
      case "create_bid_error":
        throw new UpbitException(
            UpbitErrorCode.CREATE_BID_ERROR,
            String.format("errorCode: %s, msg: %s", errorCode, serverMsg));
      case "insufficient_funds_ask":
        throw new UpbitException(
            UpbitErrorCode.INSUFFICIENT_FUNDS_ASK,
            String.format("errorCode: %s, msg: %s", errorCode, serverMsg));
      case "insufficient_funds_bid":
        throw new UpbitException(
            UpbitErrorCode.INSUFFICIENT_FUNDS_BID,
            String.format("errorCode: %s, msg: %s", errorCode, serverMsg));
      case "under_min_total_ask":
        throw new UpbitException(
            UpbitErrorCode.UNDER_MIN_TOTAL_ASK,
            String.format("errorCode: %s, msg: %s", errorCode, serverMsg));
      case "under_min_total_bid":
        throw new UpbitException(
            UpbitErrorCode.UNDER_MIN_TOTAL_BID,
            String.format("errorCode: %s, msg: %s", errorCode, serverMsg));
      case "withdraw_address_not_registerd":
        throw new UpbitException(
            UpbitErrorCode.BAD_REQUEST,
            String.format("errorCode: %s, msg: %s", errorCode, serverMsg));
      case "validation_error", "invalid_parameter":
        throw new UpbitException(
            UpbitErrorCode.INVALID_PARAMETER,
            String.format("errorCode: %s, msg: %s", errorCode, serverMsg));
      default:
        throw new UpbitException(
            UpbitErrorCode.UNKNOWN, String.format("errorCode: %s, msg: %s", errorCode, serverMsg));
    }
  }
}
