package main.arbitrage.infrastructure.oauthValidator.kakao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import main.arbitrage.infrastructure.oauthValidator.OAuthApiClient;
import main.arbitrage.infrastructure.oauthValidator.dto.OAuthValidatorDTO;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoApiClient implements OAuthApiClient {
  private final OkHttpClient okHttpClient;
  private final ObjectMapper objectMapper;
  private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

  @Override
  public OAuthValidatorDTO validateTokenAndGetUserInfo(String accessToken) {
    try {
      Request request =
          new Request.Builder()
              .url(KAKAO_USER_INFO_URL)
              .addHeader("Authorization", "Bearer " + accessToken)
              .build();

      try (Response response = okHttpClient.newCall(request).execute()) {
        if (!response.isSuccessful()) {
          throw new IllegalArgumentException("Failure Kakao API request");
        }

        JsonNode jsonNode = objectMapper.readTree(response.body().string());

        return OAuthValidatorDTO.builder()
            .email(jsonNode.get("kakao_account").get("email").asText())
            .providerId(jsonNode.get("id").asText())
            .build();
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Failure Kakao API request");
    }
  }

  @Override
  public boolean validateUser(String accessToken, String providerId, String email) {
    OAuthValidatorDTO userInfo = validateTokenAndGetUserInfo(accessToken);
    return userInfo.getProviderId().equals(providerId) && userInfo.getEmail().equals(email);
  }
}
