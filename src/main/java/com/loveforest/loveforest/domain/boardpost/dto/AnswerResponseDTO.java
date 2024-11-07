package com.loveforest.loveforest.domain.boardpost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerResponseDTO {

    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "게시글 내용", example = "오늘의 목표는 체력 관리입니다.")
    private String content;

    @Schema(description = "작성자 닉네임", example = "user123")
    private String authorNickname;

    @Schema(description = "게시글 생성일", example = "2023-01-01T12:00:00")
    private LocalDateTime createdDate;
}