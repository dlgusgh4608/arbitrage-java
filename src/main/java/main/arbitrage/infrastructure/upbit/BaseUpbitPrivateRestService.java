package main.arbitrage.infrastructure.upbit;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import main.arbitrage.infrastructure.upbit.exception.UpbitErrorCode;
import main.arbitrage.infrastructure.upbit.exception.UpbitException;

public class BaseUpbitPrivateRestService {
  protected final String accessKey;
  protected final String secretKey;
  protected final List<String> symbolNames;

  public BaseUpbitPrivateRestService(String accessKey, String secretKey, List<String> symbolNames) {
    if (accessKey.isEmpty() || secretKey.isEmpty())
      throw new UpbitException(UpbitErrorCode.EMPTY_KEYS);

    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.symbolNames = symbolNames;
  }

  public String convertSymbol(String symbol) {
    String upperSymbol = symbol.toUpperCase().replace("KRW-", "");

    if (!symbolNames.contains(upperSymbol)) throw new UpbitException(UpbitErrorCode.INVALID_SYMBOL);

    return "KRW-" + upperSymbol;
  }

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

  public String generateQueryString(Map<String, Object> params) {
    return params.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining("&"));
  }

  public String generateQueryHash(String queryString) throws Exception {
    MessageDigest md = MessageDigest.getInstance("SHA-512");
    md.update(queryString.getBytes(StandardCharsets.UTF_8));
    return String.format("%0128x", new BigInteger(1, md.digest()));
  }
}
