package main.arbitrage.domain.oauthUser.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthUserRegisterRequest {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    private String email;


    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
            message = "비밀번호는 8~30자리이면서 1개 이상의 알파벳, 숫자, 특수문자를 포함해야합니다."
    )
    private String password;

    @NotBlank(message = "Invalid value")
    private String providerId;

    @NotBlank(message = "Invalid value")
    private String provider;

    @NotBlank(message = "Invalid value")
    private String accessToken;

    @Builder
    public OAuthUserRegisterRequest(String email, String password, String providerId, String provider, String accessToken) {
        this.email = email;
        this.password = password;
        this.providerId = providerId;
        this.provider = provider;
        this.accessToken = accessToken;
    }
}