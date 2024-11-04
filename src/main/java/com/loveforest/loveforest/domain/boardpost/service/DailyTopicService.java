package com.loveforest.loveforest.domain.boardpost.service;

import com.loveforest.loveforest.domain.boardpost.entity.DailyTopic;
import com.loveforest.loveforest.domain.boardpost.exception.DailyTopicNotFoundException;
import com.loveforest.loveforest.domain.boardpost.repository.DailyTopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DailyTopicService {

    private final DailyTopicRepository dailyTopicRepository;

    public DailyTopic createDailyTopic(String content) {
        DailyTopic dailyTopic = new DailyTopic(content, LocalDate.now());
        return dailyTopicRepository.save(dailyTopic);
    }

    public DailyTopic getDailyTopicByDate(LocalDate date) {
        return dailyTopicRepository.findByDate(date)
                .orElseThrow(DailyTopicNotFoundException::new); // 예외 추가
    }

    public Optional<DailyTopic> getDailyTopicById(Long dailyTopicId) {
        return dailyTopicRepository.findById(dailyTopicId);
    }
}