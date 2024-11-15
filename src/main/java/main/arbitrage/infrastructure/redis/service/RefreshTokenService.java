package main.arbitrage.infrastructure.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${token.refresh}")
    private Long refreshTokenTTL;

    public String findRefreshToken(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    public String updateRefreshToken(String email, String newToken) {
        Long prevRefreshTokenTTL = redisTemplate.getExpire(email);

        if (prevRefreshTokenTTL > 0) {
            redisTemplate.opsForValue().set(email, newToken, Duration.ofSeconds(prevRefreshTokenTTL));

            return newToken;
        } else {
            redisTemplate.delete(email);
            throw new RuntimeException("Refresh token not found or expired");
        }
    }

    public String createRefreshToken(String email, String token) {
        redisTemplate.opsForValue().set(email, token, Duration.ofSeconds(refreshTokenTTL));

        return token;
    }
}