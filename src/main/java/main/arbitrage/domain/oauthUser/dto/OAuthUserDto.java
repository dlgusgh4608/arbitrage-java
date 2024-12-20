package main.arbitrage.domain.oauthUser.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
@Setter
public class OAuthUserDto {
    private final String provider;
    private final String providerId;
    private final String email;
    private final String accessToken;
}
