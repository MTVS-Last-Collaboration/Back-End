package com.loveforest.loveforest.domain.boardpost.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDTO {

    @Schema(description = "댓글 ID", example = "1")
    private Long id;

    @Schema(description = "게시글 ID", example = "1")
    private Long answerId;


    @Schema(description = "댓글 내용", example = "저도 그렇게 생각합니다!")
    private String content;

    @Schema(description = "작성자 닉네임", example = "user123")
    private String authorNickname;

    @Schema(description = "댓글 좋아요 수", example = "1")
    private int likeCount;

    @Schema(description = "댓글 생성일", example = "2023-01-01T12:00:00")
    private LocalDateTime createdDate;
}
