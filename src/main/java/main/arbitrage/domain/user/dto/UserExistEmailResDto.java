package main.arbitrage.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Builder;

@Getter
public class UserExistEmailResDto {
    @JsonProperty("code")
    private String code;

    @Builder
    public UserExistEmailResDto(String code) {
        this.code = code;
    }
}