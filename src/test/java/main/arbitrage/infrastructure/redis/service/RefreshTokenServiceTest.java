package main.arbitrage.infrastructure.redis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_TOKEN = "token";
    private final Long TEST_TTL = 604800L;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenTTL", TEST_TTL);
    }

    @Test
    @DisplayName("Find RefreshToken")
    void findRefreshTokenSuccessTest() {
        // given
        given(valueOperations.get(TEST_EMAIL)).willReturn(TEST_TOKEN);

        // when
        String foundToken = refreshTokenService.findRefreshToken(TEST_EMAIL);

        // then
        assertThat(foundToken).isEqualTo(TEST_TOKEN);
        verify(valueOperations).get(TEST_EMAIL);
    }

    @Test
    @DisplayName("Update RefreshToken")
    void updateRefreshTokenSuccessTest() {
        // given
        String newToken = "newToken";
        given(redisTemplate.getExpire(TEST_EMAIL)).willReturn(TEST_TTL);
        valueOperations.set(
                eq(TEST_EMAIL),
                eq(TEST_TOKEN),
                any(Duration.class)
        );

        // when
        String updatedToken = refreshTokenService.updateRefreshToken(TEST_EMAIL, newToken);

        // then
        assertThat(updatedToken).isEqualTo(newToken);
        verify(redisTemplate).getExpire(TEST_EMAIL);
        verify(valueOperations).set(
                eq(TEST_EMAIL),
                eq(newToken),
                eq(Duration.ofSeconds(TEST_TTL))
        );
    }
}