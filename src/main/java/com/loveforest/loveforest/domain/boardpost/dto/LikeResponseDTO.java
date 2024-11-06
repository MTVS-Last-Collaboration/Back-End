package com.loveforest.loveforest.domain.boardpost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeResponseDTO {
    @Schema(description = "답변 또는 댓글 ID", example = "1")
    private final Long id;          // 대상 ID (답변 또는 댓글 ID)

    @Schema(description = "현재 좋아요 수", example = "1")
    private final int likeCount;    // 현재 좋아요 수

    @Schema(description = "좋아요 상태 (추가 시 true, 취소 시 false)", example = "true")
    private final boolean liked;
}
