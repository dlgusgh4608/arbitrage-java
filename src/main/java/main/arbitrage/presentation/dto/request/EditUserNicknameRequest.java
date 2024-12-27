package main.arbitrage.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditUserNicknameRequest(@NotBlank(message = "닉네임은 필수 입력값입니다.") @Size(min = 3,
        max = 60, message = "닉네임은 60자를 초과할 수 없습니다.") String nickname) {
}
