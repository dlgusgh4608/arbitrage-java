package main.arbitrage.presentation.dto.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class UserLoginForm {

  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "이메일 형식으로 입력해주세요.")
  private String email;

  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  private String password;
}
