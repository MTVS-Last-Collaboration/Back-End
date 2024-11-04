package com.loveforest.loveforest.domain.boardpost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;


@Data
public class DailyTopicRequestDTO {

    @Schema(description = "날짜", example = "2024-12-12", required = true)
    @NotEmpty(message = "날짜 입력은 필수입니다.")
    private LocalDate date;

    @Schema(description = "토픽", example = "오늘의 목표는 무엇인가요?", required = true)
    @NotEmpty(message = "토픽 내용은 필수입니다.")
    private String content;
}
