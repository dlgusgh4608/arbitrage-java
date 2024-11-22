package main.arbitrage.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Builder;

@Getter
public class UserSendMailResponse {
    @JsonProperty("code")
    private String code;

    @Builder
    public UserSendMailResponse(String code) {
        this.code = code;
    }
}