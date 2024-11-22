package main.arbitrage.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Builder;

@Getter
public class UserTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("refresh_token_ttl")
    private Long refreshTokenTTL;

    @Builder
    public UserTokenResponse(String accessToken, String refreshToken, Long refreshTokenTTL) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.refreshTokenTTL = refreshTokenTTL;
    }
}