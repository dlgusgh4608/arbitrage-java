package main.arbitrage.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserTokenResponseCookie {
    private final String accessToken;
    private final String refreshToken;
    private final Long refreshTokenTTL;
}
