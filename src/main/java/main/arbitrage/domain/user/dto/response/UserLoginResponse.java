package main.arbitrage.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Builder;

@Getter
public class UserLoginResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @Builder
    public UserLoginResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}