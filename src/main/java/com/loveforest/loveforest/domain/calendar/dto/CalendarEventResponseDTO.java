package com.loveforest.loveforest.domain.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "캘린더 이벤트 응답 DTO")
public class CalendarEventResponseDTO {

    @Schema(description = "이벤트 ID", example = "1", required = true)
    private Long eventId;

    @Schema(description = "이벤트 이름", example = "기념일", required = true)
    private String eventName;

    @Schema(description = "아이콘 넘버", example = "1")
    private int iconNumber;

    @Schema(description = "이벤트 날짜", example = "2024-12-25", required = true)
    private LocalDate eventDate;

    @Schema(description = "이벤트 설명", example = "첫 기념일")
    private String description;
}
