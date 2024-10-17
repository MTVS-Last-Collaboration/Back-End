package com.loveforest.loveforest.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {

    @Schema(description = "상태 코드", example = "404")
    private Integer status;

    @Schema(description = "에러 타입", example = "Invalid Input")
    private String errorType;

    @Schema(description = "에러 메시지", example = "입력값이 잘못되었습니다.")
    private String message;

    @Schema(description = "에러 발생 시간", example = "2023-10-16T12:34:56")
    private LocalDateTime timestamp;
}