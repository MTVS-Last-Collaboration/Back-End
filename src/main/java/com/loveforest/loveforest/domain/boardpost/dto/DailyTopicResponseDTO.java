package com.loveforest.loveforest.domain.boardpost.dto;

import com.loveforest.loveforest.domain.boardpost.entity.DailyTopic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyTopicResponseDTO {

    @Schema(description = "토픽 ID", example = "1")
    private Long id;              // 질문 ID

    @Schema(description = "토픽 내용", example = "너희들이 다툰 경험과 방법을 말해줘!")
    private String content;       // 질문 내용

    @Schema(description = "생성일", example = "2024-11-01")
    private LocalDate date;       // 질문 생성 날짜

    // DailyTopicId 엔티티로부터 DTO로 변환하는 생성자
    public DailyTopicResponseDTO(DailyTopic dailyTopic) {
        this.id = dailyTopic.getId();
        this.content = dailyTopic.getContent();
        this.date = dailyTopic.getDate();
    }
}
