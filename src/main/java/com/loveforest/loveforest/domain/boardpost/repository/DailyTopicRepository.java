package com.loveforest.loveforest.domain.boardpost.repository;

import com.loveforest.loveforest.domain.boardpost.entity.DailyTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyTopicRepository extends JpaRepository<DailyTopic, Long> {
    Optional<DailyTopic> findByDate(LocalDate date); // 특정 날짜의 질문 조회
}
