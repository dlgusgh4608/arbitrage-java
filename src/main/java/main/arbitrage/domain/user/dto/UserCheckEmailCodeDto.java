package main.arbitrage.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCheckEmailCodeDto {
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    private String originCode;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String encryptedCode;

    @Builder
    public UserCheckEmailCodeDto(String originCode, String encryptedCode) {
        this.originCode = originCode;
        this.encryptedCode = encryptedCode;
    }
}
