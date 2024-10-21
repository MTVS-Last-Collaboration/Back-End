package com.loveforest.loveforest.domain.couple.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CoupleJoinResponseDTO {

    @Schema(description = "커플 연동 성공 메시지",
            example = "커플 연동이 성공적으로 완료되었습니다.",
            required = true)
    private String message;
}