package main.arbitrage.domain.grade.service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import main.arbitrage.domain.grade.entity.Grade;
import main.arbitrage.domain.grade.exception.GradeErrorCode;
import main.arbitrage.domain.grade.exception.GradeException;
import main.arbitrage.domain.grade.respository.GradeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GradeService {
  private final List<String> DEFAULT_GRADE = List.of("STANDARD", "BUSINESS", "FIRST", "GOD");

  private final GradeRepository gradeRepository;

  @PostConstruct
  private void init() {
    initializeGrades();
  }

  private void initializeGrades() {
    DEFAULT_GRADE.forEach(this::initializeGrade);
  }

  private void initializeGrade(String name) {
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
