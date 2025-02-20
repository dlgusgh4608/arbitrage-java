package main.arbitrage.domain.userEnv.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
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

  @Column(name = "upbit_access_key", nullable = false)
  private String upbitAccessKey;

  @Column(name = "upbit_secret_key", nullable = false)
  private String upbitSecretKey;

  @Column(name = "binance_access_key", nullable = false)
  private String binanceAccessKey;

  @Column(name = "binance_secret_key", nullable = false)
  private String binanceSecretKey;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP(6) WITH TIME ZONE")
  private Timestamp createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP(6) WITH TIME ZONE")
  private Timestamp updatedAt;

  public void updateEnv(UserEnvForm userEnvForm, AESCrypto aesCrypto) {
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
