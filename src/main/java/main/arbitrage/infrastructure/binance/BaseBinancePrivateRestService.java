package main.arbitrage.infrastructure.binance;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import main.arbitrage.infrastructure.binance.exception.BinanceErrorCode;
import main.arbitrage.infrastructure.binance.exception.BinanceException;
import org.springframework.security.crypto.codec.Hex;

public class BaseBinancePrivateRestService {
  protected final String accessKey;
  protected final String secretKey;
  protected final List<String> symbolNames;
  private static final String HASH_ALGORITHM = "HmacSHA256";

  public BaseBinancePrivateRestService(
      String accessKey, String secretKey, List<String> symbolNames) {
    if (accessKey.isEmpty() || secretKey.isEmpty())
      throw new BinanceException(BinanceErrorCode.EMPTY_KEYS);

    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.symbolNames = symbolNames;
  }

  protected String convertSymbol(String symbol) {
    String upperSymbol = symbol.toUpperCase().replace("USDT", "");

    if (!symbolNames.contains(upperSymbol)) throw new BinanceException(BinanceErrorCode.BAD_SYMBOL);

    return upperSymbol + "USDT";
  }

  protected String generateQueryHash(String queryString)
      throws NoSuchAlgorithmException, InvalidKeyException {
    Mac hmac = Mac.getInstance(HASH_ALGORITHM);
    SecretKeySpec secretKeySpec =
        new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HASH_ALGORITHM);
    hmac.init(secretKeySpec);

    byte[] signatureBytes = hmac.doFinal(queryString.getBytes(StandardCharsets.UTF_8));
    return String.valueOf(Hex.encode(signatureBytes));
  }

  protected String generateQueryString(Map<String, Object> params) {
    return params.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining("&"));
  }

  protected void generateToken(Map<String, Object> params) {
    try {
      String queryString = generateQueryString(params);
      String signature = generateQueryHash(queryString);
      params.put("signature", signature);
    } catch (Exception e) {
      throw new BinanceException(BinanceErrorCode.INVALID_SIGNATURE, e);
    }
  }
}
