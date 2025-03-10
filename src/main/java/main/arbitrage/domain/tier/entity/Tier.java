package main.arbitrage.domain.tier.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tier")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tier {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "name", nullable = false, unique = true, columnDefinition = "VARCHAR(8)")
  private TierName name;

  @Builder
  public Tier(TierName name) {
    this.name = name;
  }
}
