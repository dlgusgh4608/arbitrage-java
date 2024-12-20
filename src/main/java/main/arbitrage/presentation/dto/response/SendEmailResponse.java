package main.arbitrage.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SendEmailResponse {
    @JsonProperty("code")
    private String code;

    @Builder
    public SendEmailResponse(String code) {
        this.code = code;
    }
}
