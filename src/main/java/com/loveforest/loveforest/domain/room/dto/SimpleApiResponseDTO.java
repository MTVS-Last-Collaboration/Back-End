package com.loveforest.loveforest.domain.room.dto;

import lombok.Getter;

/**
 * 단순 성공 응답 DTO
 */
@Getter
public class SimpleApiResponseDTO extends ApiResponseDTO<Void> {
    public SimpleApiResponseDTO(String message) {
        super(message, null);
    }
}