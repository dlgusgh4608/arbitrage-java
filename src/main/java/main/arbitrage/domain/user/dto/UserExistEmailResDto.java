package main.arbitrage.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserExistEmailResDto {
    @JsonProperty("code")
    private String code;

    @Builder
    public UserExistEmailResDto(String code) {
        this.code = code;
    }
}
