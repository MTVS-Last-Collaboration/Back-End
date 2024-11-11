package com.loveforest.loveforest.domain.room.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 기본 API 응답을 위한 추상 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class ApiResponseDTO<T> {
    private String message;
    private T data;

    protected ApiResponseDTO(String message, T data) {
        this.message = message;
        this.data = data;
    }
}
