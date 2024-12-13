package main.arbitrage.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCheckEmailCodeDto {
    @NotBlank(message = "originCode is empty")
    private String originCode;

    @NotBlank(message = "encryptedCode is empty")
    private String encryptedCode;

    @Builder
    public UserCheckEmailCodeDto(String originCode, String encryptedCode) {
        this.originCode = originCode;
        this.encryptedCode = encryptedCode;
    }
}
