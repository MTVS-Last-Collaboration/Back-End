package com.loveforest.loveforest.domain.boardpost.service;

import com.loveforest.loveforest.domain.boardpost.dto.DailyTopicResponseDTO;
import com.loveforest.loveforest.domain.boardpost.entity.DailyTopic;
import com.loveforest.loveforest.domain.boardpost.exception.DailyTopicAlreadyExistsException;
import com.loveforest.loveforest.domain.boardpost.exception.DailyTopicNotFoundException;
import com.loveforest.loveforest.domain.boardpost.repository.DailyTopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<DailyTopicResponseDTO> getAllDailyTopic() {
        List<DailyTopic> dailyTopicList = dailyTopicRepository.findAll();

        return dailyTopicList.stream()
                .map(dailyTopic -> new DailyTopicResponseDTO(
                        dailyTopic.getId(),
                        dailyTopic.getContent(),
                        dailyTopic.getDate()
                ))
                .toList();
    }
}