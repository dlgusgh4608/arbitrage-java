package main.arbitrage.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEditNicknameDto {

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    @Size(min = 3, max = 60, message = "닉네임은 60자를 초과할 수 없습니다.")
    private String nickname;
}
