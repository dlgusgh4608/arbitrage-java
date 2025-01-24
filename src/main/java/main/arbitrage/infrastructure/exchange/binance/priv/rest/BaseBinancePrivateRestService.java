package main.arbitrage.infrastructure.exchange.binance.priv.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import main.arbitrage.infrastructure.exchange.ExchangeRestService;
import main.arbitrage.infrastructure.exchange.binance.exception.BinanceErrorCode;
import main.arbitrage.infrastructure.exchange.binance.exception.BinanceException;
import okhttp3.OkHttpClient;
import org.springframework.security.crypto.codec.Hex;

public class BaseBinancePrivateRestService implements ExchangeRestService {
  protected final String accessKey;
  protected final String secretKey;
  protected final OkHttpClient okHttpClient;
  protected final ObjectMapper objectMapper;
  protected final List<String> symbolNames;
  protected static final String HASH_ALGORITHM = "HmacSHA256";

  protected final String DEFAULT_URL = "https://fapi.binance.com/fapi";

  public BaseBinancePrivateRestService(
      String accessKey,
      String secretKey,
      OkHttpClient okHttpClient,
      ObjectMapper objectMapper,
      List<String> symbolNames) {
    if (accessKey.isEmpty() || secretKey.isEmpty()) {
      throw new BinanceException(BinanceErrorCode.EMPTY_KEYS);
    }
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.okHttpClient = okHttpClient;
    this.objectMapper = objectMapper;
    this.symbolNames = symbolNames;
  }

  @Override
  public String convertSymbol(String symbol) {
    String upperSymbol = symbol.toUpperCase().replace("USDT", "");

    if (!symbolNames.contains(upperSymbol)) throw new BinanceException(BinanceErrorCode.BAD_SYMBOL);

    return upperSymbol + "USDT";
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

  @Override
  public String generateQueryString(Map<String, Object> params) {
    return params.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining("&"));
  }

  @Override
  public String generateToken() {
    try {
      Map<String, Object> params = new LinkedHashMap<>();
      String signature = generateQueryHash("");
      params.put("signature", signature);

      return generateQueryString(params);
    } catch (Exception e) {
      throw new BinanceException(BinanceErrorCode.INVALID_SIGNATURE, e);
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
      throw new BinanceException(BinanceErrorCode.INVALID_SIGNATURE, e);
    }
  }

  @Override
  public void validateResponse(JsonNode json) {
    String errorCode = json.get("code").asText();
    switch (errorCode) {
      // General Server or Network issues (10xx)
      case "-1000":
        throw new BinanceException(BinanceErrorCode.UNKNOWN);
      case "-1001":
        throw new BinanceException(BinanceErrorCode.DISCONNECTED);
      case "-1002":
        throw new BinanceException(BinanceErrorCode.UNAUTHORIZED);
      case "-1008":
        throw new BinanceException(BinanceErrorCode.TOO_MANY_REQUESTS);
      case "-1021":
        throw new BinanceException(BinanceErrorCode.INVALID_TIMESTAMP);
      case "-1022":
        throw new BinanceException(BinanceErrorCode.INVALID_SIGNATURE);

      // Request issues (11xx - 2xxx)
      case "-1102":
        throw new BinanceException(BinanceErrorCode.MANDATORY_PARAM_EMPTY_OR_MALFORMED);
      case "-1121":
        throw new BinanceException(BinanceErrorCode.BAD_SYMBOL);
      case "-2010":
        throw new BinanceException(BinanceErrorCode.NEW_ORDER_REJECTED);
      case "-2013":
        throw new BinanceException(BinanceErrorCode.NO_SUCH_ORDER);
      case "-2014":
        throw new BinanceException(BinanceErrorCode.BAD_API_KEY_FMT);
      case "-2015":
        throw new BinanceException(BinanceErrorCode.INVALID_API_KEY_IP_PERMISSION);
      case "-2018":
        throw new BinanceException(BinanceErrorCode.BALANCE_NOT_SUFFICIENT);

      // Filters and other issues (3xxx-5xxx)
      case "-4001":
        throw new BinanceException(BinanceErrorCode.PRICE_LESS_THAN_ZERO);
      case "-4003":
        throw new BinanceException(BinanceErrorCode.QTY_LESS_THAN_ZERO);
      case "-4055":
        throw new BinanceException(BinanceErrorCode.AMOUNT_MUST_BE_POSITIVE);
      default:
        throw new BinanceException(BinanceErrorCode.UNKNOWN);
    }
  }
}
