package com.loveforest.loveforest.domain.boardpost.repository;

import com.loveforest.loveforest.domain.boardpost.entity.Answer;
import com.loveforest.loveforest.domain.boardpost.entity.DailyTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByDailyTopic(DailyTopic dailyTopic); // 특정 질문에 대한 답변 조회
}