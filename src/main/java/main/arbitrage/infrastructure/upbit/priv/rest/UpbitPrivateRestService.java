package main.arbitrage.infrastructure.upbit.priv.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.security.WeakKeyException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import main.arbitrage.infrastructure.upbit.priv.rest.dto.UpbitGetAccountResponseDto;
import main.arbitrage.infrastructure.upbit.priv.rest.exception.UpbitPrivateRestException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class UpbitPrivateRestService {
    private final String accessKey;
    private final String secretKey;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    private final String SERVER_URI = "https://api.upbit.com";

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

    private void validateResponse(String responseBody) throws UpbitPrivateRestException, JsonProcessingException {
        JsonNode json = objectMapper.readTree(responseBody);
        String errorCode = json.get("error").get("name").asText();
        switch (errorCode) {
            case "invalid_query_payload":
                throw new UpbitPrivateRestException("JWT 헤더의 페이로드가 올바르지 않습니다.", errorCode);
            case "jwt_verification":
                throw new UpbitPrivateRestException("JWT 헤더 검증에 실패했습니다.", errorCode);
            case "expired_access_key":
                throw new UpbitPrivateRestException("API 키가 만료되었습니다.", errorCode);
            case "nonce_used":
                throw new UpbitPrivateRestException("이미 사용된 nonce값입니다.", errorCode);
            case "no_authorization_i_p":
                throw new UpbitPrivateRestException("허용되지 않은 IP 주소입니다.", errorCode);
            case "out_of_scope":
                throw new UpbitPrivateRestException("허용되지 않은 기능입니다.", errorCode);
            default:
                throw new UpbitPrivateRestException("알 수 없는 에러가 발생했습니다.", "UNKNOWN_ERROR");
        }
    }

    private String generateToken() {
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
            throw new UpbitPrivateRestException("JWT 헤더 검증에 실패했습니다.", "jwt_verification");
        }
    }

    private String generateToken(String queryString) {
        try {
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
            throw new UpbitPrivateRestException("JWT 헤더 검증에 실패했습니다.", "jwt_verification");
        }
    }

    private String generateQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    private String generateQueryHash(String queryString) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes(StandardCharsets.UTF_8));
        return String.format("%0128x", new BigInteger(1, md.digest()));

    }
}