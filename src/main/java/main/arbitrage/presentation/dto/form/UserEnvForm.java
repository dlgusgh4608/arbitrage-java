package main.arbitrage.presentation.dto.form;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.userEnv.entity.UserEnv;
import main.arbitrage.global.util.aes.AESCrypto;


@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class UserEnvForm {

    @NotBlank(message = "업비트 액세스키는 필수 값 입니다.")
    private String upbitAccessKey;

    @NotBlank(message = "업비트 시크릿키는 필수 값 입니다.")
    private String upbitSecretKey;

    @NotBlank(message = "바이낸스 액세스키는 필수 값 입니다.")
    private String binanceAccessKey;

    @NotBlank(message = "바이낸스 시크릿키는 필수 값 입니다.")
    private String binanceSecretKey;

    public static UserEnv toEntity(UserEnvForm userEnvDto, User user, AESCrypto aesCrypto)
            throws Exception {
        return UserEnv.builder().user(user)
                .upbitAccessKey(aesCrypto.encrypt(userEnvDto.getUpbitAccessKey().getBytes()))
                .upbitSecretKey(aesCrypto.encrypt(userEnvDto.getUpbitSecretKey().getBytes()))
                .binanceAccessKey(aesCrypto.encrypt(userEnvDto.getBinanceAccessKey().getBytes()))
                .binanceSecretKey(aesCrypto.encrypt(userEnvDto.getBinanceSecretKey().getBytes()))
                .build();
    }
}
