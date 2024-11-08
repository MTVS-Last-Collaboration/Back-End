package com.loveforest.loveforest.domain.boardpost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerRequestDTO {

    @Schema(description = "토픽 ID", example = "1", required = true)
    @NotNull(message = "토픽 ID는 필수 입력 값입니다.")
    private Long dailyTopicId;

    @Schema(description = "게시글 제목", example = "오늘의 목표.", required = true)
    @NotBlank(message = "게시글 제목은 필수 입력 값입니다.")
    private String title;

    @Schema(description = "게시글 내용", example = "오늘의 목표는 체력 관리입니다.", required = true)
    @NotBlank(message = "게시글 내용은 필수 입력 값입니다.")
    private String content;
}