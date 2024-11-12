package com.loveforest.loveforest.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageRequestDTO {
    @Schema(description = "발신자의 ID", example = "1")
    private Long senderId; // 발신자 ID

    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String messages; // 메시지 내용

    @Schema(description = "커플의 ID", example = "1")
    private Long coupleId; // 발신자 ID

    @Schema(description = "반려몽 레벨", example = "1~20")
    private int petLevel; // 반려몽 레벨
}