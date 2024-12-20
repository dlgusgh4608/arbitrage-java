package main.arbitrage.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserTokenDto {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("refresh_token_ttl")
    private Long refreshTokenTTL;

    @Builder
    public UserTokenDto(String accessToken, String refreshToken, Long refreshTokenTTL) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.refreshTokenTTL = refreshTokenTTL;
    }
}
