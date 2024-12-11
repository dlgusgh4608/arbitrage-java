package main.arbitrage.domain.userEnv.entity;

import jakarta.persistence.*;
import lombok.*;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.domain.userEnv.dto.UserEnvDto;
import main.arbitrage.global.util.aes.AESCrypto;

@Entity
@Table(name = "user_env")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEnv {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "upbit_access_key")
    private String upbitAccessKey;

    @Column(name = "upbit_secret_key")
    private String upbitSecretKey;

    @Column(name = "binance_access_key")
    private String binanceAccessKey;

    @Column(name = "binance_secret_key")
    private String binanceSecretKey;

    public void updateEnv(UserEnvDto userEnvDto, AESCrypto aesCrypto) throws Exception {
        this.upbitAccessKey = aesCrypto.encrypt(userEnvDto.getUpbitAccessKey().getBytes());
        this.upbitSecretKey = aesCrypto.encrypt(userEnvDto.getUpbitSecretKey().getBytes());
        this.binanceAccessKey = aesCrypto.encrypt(userEnvDto.getBinanceAccessKey().getBytes());
        this.binanceSecretKey = aesCrypto.encrypt(userEnvDto.getBinanceSecretKey().getBytes());
    }


    @Builder
    public UserEnv(
            User user,
            String upbitAccessKey,
            String upbitSecretKey,
            String binanceAccessKey,
            String binanceSecretKey
    ) {
        this.user = user;
        this.upbitAccessKey = upbitAccessKey;
        this.upbitSecretKey = upbitSecretKey;
        this.binanceAccessKey = binanceAccessKey;
        this.binanceSecretKey = binanceSecretKey;
    }

}