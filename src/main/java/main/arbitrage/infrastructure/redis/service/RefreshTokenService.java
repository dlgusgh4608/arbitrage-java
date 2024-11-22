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

    public Long getRefreshTokenTTL(String email) {
        return redisTemplate.getExpire(email);
    }

    public String updateRefreshToken(String email, String newToken, Long TTL) {
        redisTemplate.opsForValue().set(email, newToken, Duration.ofSeconds(TTL));
        return newToken;
    }

    public String createRefreshToken(String email, String token) {
        redisTemplate.opsForValue().set(email, token, Duration.ofSeconds(refreshTokenTTL));

        return token;
    }
}