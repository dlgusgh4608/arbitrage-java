package main.arbitrage.presentation.dto.view;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthSignupView {
  private final String provider;
  private final String providerId;
  private final String email;
  private final String accessToken;
}
