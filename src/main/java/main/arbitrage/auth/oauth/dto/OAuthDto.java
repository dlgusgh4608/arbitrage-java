package main.arbitrage.auth.oauth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
@Setter
public class OAuthDto {
    private final String provider;
    private final String providerId;
    private final String email;
}