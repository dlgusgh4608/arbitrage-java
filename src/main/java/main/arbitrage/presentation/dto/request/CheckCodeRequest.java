package main.arbitrage.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckCodeRequest {
    @NotBlank(message = "originCode is empty")
    private String originCode;

    @NotBlank(message = "encryptedCode is empty")
    private String encryptedCode;

    @Builder
    public CheckCodeRequest(String originCode, String encryptedCode) {
        this.originCode = originCode;
        this.encryptedCode = encryptedCode;
    }
}
