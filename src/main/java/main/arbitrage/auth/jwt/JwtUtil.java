package main.arbitrage.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import main.arbitrage.auth.jwt.dto.JwtDto;
import main.arbitrage.auth.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtUtil {
    private SecretKey key;

    @Value("${token.access}")
    private Long tokenTTL;

    public JwtUtil(
            @Value("${jwt.secret}") String secretKey
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Long userId, String email) {
        Map<String, String> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("email", email);

        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenTTL);

        return Jwts
                .builder()
                .subject("access")
                .claims(claims)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public JwtDto getTokenInfo(String token) {
        try {
            Claims jwtClaim = Jwts
                    .parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return JwtDto.builder()
                    .userId(Long.parseLong(jwtClaim.get("userId", String.class)))
                    .email(jwtClaim.get("email", String.class))
                    .isExpired(false)
                    .build();
        } catch (ExpiredJwtException e) { // access token이 만료되었을때에도 사용자값 return
            Claims jwtClaim = e.getClaims();

            return JwtDto.builder()
                    .userId(Long.parseLong(jwtClaim.get("userId", String.class)))
                    .email(jwtClaim.get("email", String.class))
                    .isExpired(true)
                    .build();
        } catch (Exception e) {
            log.info("Invalid JWT, {}", e.getMessage());
            throw new RuntimeException("invalid Token");
        }

    }
}