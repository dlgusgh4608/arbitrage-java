package main.arbitrage.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.arbitrage.domain.grade.entity.Grade;
import main.arbitrage.domain.grade.entity.GradeName;
import main.arbitrage.domain.tier.entity.Tier;
import main.arbitrage.domain.user.exception.UserErrorCode;
import main.arbitrage.domain.user.exception.UserException;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "`user`")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "email", nullable = false, unique = true, columnDefinition = "VARCHAR(100)")
  private String email;

  @Column(name = "nickname", nullable = false, unique = true, columnDefinition = "VARCHAR(60)")
  private String nickname;

  @Column(name = "password", nullable = false, columnDefinition = "CHAR(60)")
  private String password;

  @Column(name = "lp_flag", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
  private boolean lpFlag;

  @Column(name = "auto_flag", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
  private boolean autoFlag;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "grade_id", referencedColumnName = "id", nullable = false)
  private Grade grade;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tier_id", referencedColumnName = "id", nullable = false)
  private Tier tier;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Timestamp createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Timestamp updatedAt;

  @Builder
  public User(String password, String email, String nickname, Tier tier, Grade grade) {
    this.password = password;
    this.email = email;
    this.nickname = nickname;
    this.tier = tier;
    this.grade = grade;
  }

  public User disableLpFlag() {
    this.lpFlag = false;
    this.autoFlag = false;
    return this;
  }

  public User disableAutoFlag() {
    this.autoFlag = false;
    return this;
  }

  public User enableLpFlag() {
    if (this.grade.getName() == GradeName.STANDARD) {
      throw new UserException(UserErrorCode.USED_FORBIDDEN);
    }
    this.lpFlag = true;
    return this;
  }

  public User enableAutoFlag() {
    if (this.grade.getName() == GradeName.STANDARD || this.grade.getName() == GradeName.BUSINESS) {
      throw new UserException(UserErrorCode.USED_FORBIDDEN);
    }
    this.lpFlag = true;
    this.autoFlag = true;
    return this;
  }

  public User updateUserNickname(String nickname) {
    this.nickname = nickname;
    return this;
  }
}
