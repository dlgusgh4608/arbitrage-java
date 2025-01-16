package main.arbitrage.domain.grade.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.grade.entity.Grade;
import main.arbitrage.domain.grade.entity.GradeName;
import main.arbitrage.domain.grade.exception.GradeErrorCode;
import main.arbitrage.domain.grade.exception.GradeException;
import main.arbitrage.domain.grade.repository.GradeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GradeService {
  private final GradeRepository gradeRepository;

  @PostConstruct
  private void init() {
    for (GradeName name : GradeName.values()) {
      try {
        if (!gradeRepository.existsByName(name)) {
          gradeRepository.save(Grade.builder().name(name).build());
        }
      } catch (Exception e) {
        throw new GradeException(
            GradeErrorCode.INITIALIZED_FAILED, String.format("등급 '%s' 초기화 중 오류 발생", name), e);
      }
    }
  }

  public Grade getDefaultGrade() {
    try {
      return gradeRepository
          .findByName(GradeName.STANDARD)
          .orElseThrow(() -> new GradeException(GradeErrorCode.NOT_FOUND));
    } catch (GradeException e) {
      throw e;
    } catch (Exception e) {
      throw new GradeException(GradeErrorCode.UNKNOWN, e);
    }
  }
}
