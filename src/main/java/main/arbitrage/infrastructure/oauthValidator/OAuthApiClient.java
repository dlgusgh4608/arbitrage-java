package main.arbitrage.infrastructure.oauthValidator;

import main.arbitrage.infrastructure.oauthValidator.dto.OAuthValidatorDTO;

public interface OAuthApiClient {
  OAuthValidatorDTO validateTokenAndGetUserInfo(String accessToken);

  boolean validateUser(String accessToken, String sub, String email);
}
