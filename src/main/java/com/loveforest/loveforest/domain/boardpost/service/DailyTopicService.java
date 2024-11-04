package com.loveforest.loveforest.domain.boardpost.service;

import com.loveforest.loveforest.domain.boardpost.entity.DailyTopic;
import com.loveforest.loveforest.domain.boardpost.exception.DailyTopicAlreadyExistsException;
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

    public DailyTopic createDailyTopic(String content, LocalDate date) {

        // 중복 확인 로직 추가
        if (dailyTopicRepository.existsByDate(date)) {
            throw new DailyTopicAlreadyExistsException();
        }

        DailyTopic dailyTopic = new DailyTopic(content, date);
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