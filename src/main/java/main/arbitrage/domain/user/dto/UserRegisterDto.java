package main.arbitrage.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRegisterDto {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 30, message = "이메일은 30자를 초과할 수 없습니다.")
    private String email;


    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
            message = "비밀번호는 8~30자리이면서 1개 이상의 알파벳, 숫자, 특수문자를 포함해야합니다."
    )
    private String password;

    @NotBlank(message = "인증코드는 필수 입력값입니다.")
    private String code;

    @NotBlank(message = "Bad Request")
    private String encryptedCode;

    @Builder
    public UserRegisterDto(String email, String password, String code, String encryptedCode) {
        this.email = email;
        this.password = password;
        this.code = code;
        this.encryptedCode = encryptedCode;
    }
}