package main.arbitrage.presentation.dto.view;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
@Setter
public class OAuthSignupView {
    private final String provider;
    private final String providerId;
    private final String email;
    private final String accessToken;
}
