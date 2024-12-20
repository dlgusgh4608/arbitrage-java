package main.arbitrage.infrastructure.oauthValidator;

import main.arbitrage.infrastructure.oauthValidator.dto.OAuthValidatorDto;

public interface OAuthApiClient {
    OAuthValidatorDto validateTokenAndGetUserInfo(String accessToken);

    boolean validateUser(String accessToken, String sub, String email);
}
