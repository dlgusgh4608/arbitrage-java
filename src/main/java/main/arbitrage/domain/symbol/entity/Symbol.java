package main.arbitrage.domain.symbol.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "symbol")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Symbol {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "symbol", nullable = false, unique = true, columnDefinition = "VARCHAR(5)")
  private String name;

  @Column(name = "use", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
  private boolean use;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Builder
  public Symbol(String name, boolean use) {
    this.name = name;
    this.use = use;
  }
}
