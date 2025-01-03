package main.arbitrage.infrastructure.oauthValidator.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthValidatorDTO {
  private final String email;
  private final String providerId;
}
