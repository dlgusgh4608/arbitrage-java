package main.arbitrage.auth.oauth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OAuthDto {
    private final String provider;
    private final String providerId;
    private final String email;
}