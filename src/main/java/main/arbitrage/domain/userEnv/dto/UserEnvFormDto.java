package main.arbitrage.domain.userEnv.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserEnvFormDto {

    @NotEmpty(message = "업비트 액세스키는 필수 값 입니다.")
    private String upbitAccessKey;

    @NotEmpty(message = "업비트 시크릿키는 필수 값 입니다.")
    private String upbitSecretKey;

    @NotEmpty(message = "바이낸스 액세스키는 필수 값 입니다.")
    private String binanceAccessKey;

    @NotEmpty(message = "바이낸스 시크릿키는 필수 값 입니다.")
    private String binanceSecretKey;
}