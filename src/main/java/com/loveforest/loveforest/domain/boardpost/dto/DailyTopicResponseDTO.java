package com.loveforest.loveforest.domain.boardpost.dto;

import com.loveforest.loveforest.domain.boardpost.entity.DailyTopic;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyTopicResponseDTO {

    private Long id;              // 질문 ID
    private String content;       // 질문 내용
    private LocalDate date;       // 질문 생성 날짜

    // DailyTopicId 엔티티로부터 DTO로 변환하는 생성자
    public DailyTopicResponseDTO(DailyTopic dailyTopic) {
        this.id = dailyTopic.getId();
        this.content = dailyTopic.getContent();
        this.date = dailyTopic.getDate();
    }
}
