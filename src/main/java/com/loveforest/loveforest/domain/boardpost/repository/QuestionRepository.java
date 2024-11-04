package com.loveforest.loveforest.domain.boardpost.repository;

import com.loveforest.loveforest.domain.boardpost.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    Optional<Question> findByDate(LocalDate date); // 특정 날짜의 질문 조회
}
