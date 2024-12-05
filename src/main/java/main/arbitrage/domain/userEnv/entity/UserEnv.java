package main.arbitrage.domain.userEnv.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_env")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEnv {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_env_id", nullable = false)
    private Long userEnvId;

    @Column(name = "upbit_access_key")
    private String upbitAccessKey;

    @Column(name = "upbit_secret_key")
    private String upbitSecretKey;

    @Column(name = "binance_access_key")
    private String binanceAccessKey;

    @Column(name = "binance_secret_key")
    private String binanceSecretKey;

    @Builder
    public UserEnv(
            String upbitAccessKey,
            String upbitSecretKey,
            String binanceAccessKey,
            String binanceSecretKey
    ) {
        this.upbitAccessKey = upbitAccessKey;
        this.upbitSecretKey = upbitSecretKey;
        this.binanceAccessKey = binanceAccessKey;
        this.binanceSecretKey = binanceSecretKey;
    }

}