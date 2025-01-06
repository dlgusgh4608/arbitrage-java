package main.arbitrage.infrastructure.oauthValidator.service;

import lombok.RequiredArgsConstructor;
import main.arbitrage.infrastructure.oauthValidator.exception.OauthValidatorErrorCode;
import main.arbitrage.infrastructure.oauthValidator.exception.OauthValidatorException;
import main.arbitrage.infrastructure.oauthValidator.google.GoogleApiClient;
import main.arbitrage.infrastructure.oauthValidator.kakao.KakaoApiClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OauthValidatorService {
  private final GoogleApiClient googleApiClient;
  private final KakaoApiClient kakaoApiClient;

  public void validate(String provider, String accessToken, String providerId, String email) {
    switch (provider.toLowerCase()) {
      case "google" -> {
        if (!googleApiClient.validateUser(accessToken, providerId, email))
          throw new OauthValidatorException(OauthValidatorErrorCode.UN_SUCCESS);
      }
      case "kakao" -> {
        if (!kakaoApiClient.validateUser(accessToken, providerId, email))
          throw new OauthValidatorException(OauthValidatorErrorCode.UN_SUCCESS);
      }
      default -> {
        throw new OauthValidatorException(OauthValidatorErrorCode.INVALID_PROVIDER);
      }
    }
  }
}
