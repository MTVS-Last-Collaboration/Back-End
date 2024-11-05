package com.loveforest.loveforest.domain.chat.dto;

import com.loveforest.loveforest.domain.chat.entity.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChatMessageDTO {
    @Schema(description = "메시지 ID", example = "1")
    private Long id;

    @Schema(description = "발신자 ID", example = "123")
    private Long senderId;

    @Schema(description = "사용자가 보낸 메시지", example = "안녕하세요!")
    private String message;

    @Schema(description = "AI 서버의 응답", example = "안녕하세요! 무엇을 도와드릴까요?")
    private String response;

    @Schema(description = "메시지 전송 시간", example = "2024-11-05T16:29:32")
    private LocalDateTime timestamp;

    // ChatMessage 엔티티에서 DTO로 변환하는 메서드
    public static ChatMessageDTO fromEntity(ChatMessage chatMessage) {
        return new ChatMessageDTO(
                chatMessage.getId(),
                chatMessage.getSenderId(),
                chatMessage.getMessage(),
                chatMessage.getResponse(),
                chatMessage.getTimestamp()
        );
    }
}