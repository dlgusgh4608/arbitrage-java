package main.arbitrage.infrastructure.oauthValidator;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import main.arbitrage.infrastructure.oauthValidator.dto.OAuthValidatorDTO;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleApiClient {
  private final OAuthApiClient oAuthApiClient;

  public OAuthValidatorDTO validateTokenAndGetUserInfo(String accessToken) {
    JsonNode json = oAuthApiClient.getGoogleInfo("Bearer " + accessToken);

    return OAuthValidatorDTO.builder()
        .email(json.get("email").asText())
        .providerId(json.get("sub").asText())
        .build();
  }

  public boolean validateUser(String accessToken, String providerId, String email) {
    OAuthValidatorDTO userInfo = validateTokenAndGetUserInfo(accessToken);
    return userInfo.getProviderId().equals(providerId) && userInfo.getEmail().equals(email);
  }
}
