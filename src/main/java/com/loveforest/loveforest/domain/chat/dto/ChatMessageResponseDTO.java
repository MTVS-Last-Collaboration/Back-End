package com.loveforest.loveforest.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class ChatMessageResponseDTO {

    @Schema(description = "발신자 ID", example = "1")
    private Long senderId;

    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;

    @Schema(description = "AI 서버의 응답", example = "안녕하세요! 무엇을 도와드릴까요?")
    private String aiResponse;

    // 생성자, getter, setter 생략

    public ChatMessageResponseDTO(Long senderId, Long coupleId, String aiResponse) {
        this.senderId = senderId;
        this.coupleId = coupleId;
        this.aiResponse = aiResponse;
    }

    public ChatMessageResponseDTO(String aiResponse) {
        this.aiResponse = aiResponse;
    }

}
