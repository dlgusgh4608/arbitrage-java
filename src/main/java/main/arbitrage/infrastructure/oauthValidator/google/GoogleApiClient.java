package main.arbitrage.infrastructure.oauthValidator.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleApiClient {
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    public GoogleUserInfoDto validateTokenAndGetUserInfo(String accessToken) {
        try {
            Request request = new Request.Builder()
                    .url(GOOGLE_USER_INFO_URL)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IllegalArgumentException("Failure Google API request");
                }
                return objectMapper.readValue(response.body().string(), GoogleUserInfoDto.class);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failure Google API request");
        }
    }

    public boolean validateUser(String accessToken, String sub, String email) {
        GoogleUserInfoDto userInfo = validateTokenAndGetUserInfo(accessToken);
        return userInfo.getSub().equals(sub) && userInfo.getEmail().equals(email);
    }
}