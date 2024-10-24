package com.loveforest.loveforest.domain.chat.dto;

import lombok.Getter;

@Getter
public class ChatMessageResponseDTO {

    private String aiResponse;

    // 생성자, getter, setter 생략

    public ChatMessageResponseDTO(String aiResponse) {
        this.aiResponse = aiResponse;
    }

}
