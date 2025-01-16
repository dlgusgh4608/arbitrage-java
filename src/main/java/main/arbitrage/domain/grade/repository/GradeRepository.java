package main.arbitrage.domain.grade.repository;

import main.arbitrage.domain.grade.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
  boolean existsByName(String name);
}
