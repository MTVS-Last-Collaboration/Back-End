package com.loveforest.loveforest.domain.couple.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "커플 정보 응답 DTO")
public class CoupleResponseDTO {

    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;

    @Schema(description = "커플 코드", example = "ABC123")
    private String coupleCode;

    @Schema(description = "커플 포인트", example = "100")
    private int points;

    @Schema(description = "연애 시작일", example = "2024-01-01")
    private String anniversaryDate;
}