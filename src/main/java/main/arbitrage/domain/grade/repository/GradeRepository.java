package main.arbitrage.domain.grade.repository;

import java.util.Optional;
import main.arbitrage.domain.grade.entity.Grade;
import main.arbitrage.domain.grade.entity.GradeName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
  boolean existsByName(GradeName name);

  Optional<Grade> findByName(GradeName name);
}
