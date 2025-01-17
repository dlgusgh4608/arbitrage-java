package main.arbitrage.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.auth.dto.AuthContext;
import main.arbitrage.auth.exception.AuthErrorCode;
import main.arbitrage.auth.exception.AuthException;
import main.arbitrage.domain.grade.entity.GradeName;
import main.arbitrage.domain.tier.entity.TierName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtil {
  private SecretKey key;

  @Value("${token.access}")
  private Long tokenTTL;

  public JwtUtil(@Value("${jwt.secret}") String secretKey) {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  public String createToken(
      Long userId, String email, String nickname, TierName tier, GradeName grade) {
    Map<String, String> claims =
        Map.of(
            "userId", userId.toString(),
            "email", email,
            "nickname", nickname,
            "tier", tier.name(),
            "grade", grade.name());

    Date now = new Date();
    Date validity = new Date(now.getTime() + tokenTTL);

    return Jwts.builder()
        .subject("access")
        .claims(claims)
        .issuedAt(now)
        .expiration(validity)
        .signWith(key)
        .compact();
  }

  public String createToken(
      Long userId, String email, String nickname, TierName tier, GradeName grade, Long restTTL) {
    Map<String, String> claims =
        Map.of(
            "userId", userId.toString(),
            "email", email,
            "nickname", nickname,
            "tier", tier.name(),
            "grade", grade.name());

    Date now = new Date();
    Date validity = new Date(restTTL);

    return Jwts.builder()
        .subject("access")
        .claims(claims)
        .issuedAt(now)
        .expiration(validity)
        .signWith(key)
        .compact();
  }

  public AuthContext getTokenInfo(String token) {
    try {
      Claims jwtClaim = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

      return AuthContext.builder()
          .userId(Long.parseLong(jwtClaim.get("userId", String.class)))
          .email(jwtClaim.get("email", String.class))
          .nickname(jwtClaim.get("nickname", String.class))
          .tier(jwtClaim.get("tier", String.class))
          .grade(jwtClaim.get("grade", String.class))
          .expiredAt(jwtClaim.get("exp", Long.class))
          .build();
    } catch (ExpiredJwtException e) { // access token이 만료되었을때에도 사용자값 return
      Claims jwtClaim = e.getClaims();

      return AuthContext.builder()
          .userId(Long.parseLong(jwtClaim.get("userId", String.class)))
          .email(jwtClaim.get("email", String.class))
          .nickname(jwtClaim.get("nickname", String.class))
          .tier(jwtClaim.get("tier", String.class))
          .grade(jwtClaim.get("grade", String.class))
          .expiredAt(jwtClaim.get("exp", Long.class))
          .build();
    } catch (Exception e) {
      throw new AuthException(AuthErrorCode.INVALID_TOKEN, "액세스 토큰이 올바르지 않습니다.", e);
    }
  }
}
