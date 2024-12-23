package main.arbitrage.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CheckCodeRequest {
    @NotBlank(message = "originCode is empty")
    private String originCode;

    @NotBlank(message = "encryptedCode is empty")
    private String encryptedCode;
}
