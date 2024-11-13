package com.loveforest.loveforest.domain.photoAlbum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "API 응답 DTO")
public class ApiResponseDTO<T> {
    @Schema(description = "응답 메시지")
    private String message;

    @Schema(description = "응답 데이터")
    private T data;

    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return ApiResponseDTO.<T>builder()
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponseDTO<T> success(T data) {
        return success("성공적으로 처리되었습니다.", data);
    }
}