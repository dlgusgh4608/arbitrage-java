package main.arbitrage.domain.grade.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "grade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Grade {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name", nullable = false, unique = true, columnDefinition = "VARCHAR(8)")
  private String name;

  @Builder
  public Grade(String name) {
    this.name = name;
  }
}
