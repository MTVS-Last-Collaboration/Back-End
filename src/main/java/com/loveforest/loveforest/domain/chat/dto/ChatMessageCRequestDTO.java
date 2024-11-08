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
public class ChatMessageCRequestDTO {

    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String messages; // 메시지 내용

    @Schema(description = "반려몽 레벨", example = "1")
    private Long petLevel; // 반려몽 레벨
}
