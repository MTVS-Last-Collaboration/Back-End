package com.loveforest.loveforest.domain.boardpost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequestDTO {

    @Schema(description = "답변 ID", example = "1", required = true)
    @NotNull(message = "답변 ID는 필수 입력 값입니다.")
    private Long answerId;

    @Schema(description = "댓글 내용", example = "저도 그렇게 생각합니다!", required = true)
    @NotBlank(message = "댓글 내용은 필수 입력 값입니다.")
    private String content;
}
