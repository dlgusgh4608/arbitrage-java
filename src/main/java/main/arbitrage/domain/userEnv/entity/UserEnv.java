package main.arbitrage.domain.userEnv.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.arbitrage.domain.user.entity.User;
import main.arbitrage.global.util.aes.AESCrypto;
import main.arbitrage.presentation.dto.form.UserEnvForm;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public void updateEnv(UserEnvForm userEnvForm, AESCrypto aesCrypto) throws Exception {
    this.upbitAccessKey = aesCrypto.encrypt(userEnvForm.getUpbitAccessKey().getBytes());
    this.upbitSecretKey = aesCrypto.encrypt(userEnvForm.getUpbitSecretKey().getBytes());
    this.binanceAccessKey = aesCrypto.encrypt(userEnvForm.getBinanceAccessKey().getBytes());
    this.binanceSecretKey = aesCrypto.encrypt(userEnvForm.getBinanceSecretKey().getBytes());
  }

  @Builder
  public UserEnv(
      User user,
      String upbitAccessKey,
      String upbitSecretKey,
      String binanceAccessKey,
      String binanceSecretKey) {
    this.user = user;
    this.upbitAccessKey = upbitAccessKey;
    this.upbitSecretKey = upbitSecretKey;
    this.binanceAccessKey = binanceAccessKey;
    this.binanceSecretKey = binanceSecretKey;
  }
}
