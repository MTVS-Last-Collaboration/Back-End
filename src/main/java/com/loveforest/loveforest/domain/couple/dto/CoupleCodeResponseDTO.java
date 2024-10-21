package com.loveforest.loveforest.domain.couple.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "커플 코드 생성에 대한 응답 DTO")
public class CoupleCodeResponseDTO {

    @Schema(description = "생성된 커플 코드. 첫 번째 사용자가 커플을 생성할 때 부여된 고유 코드입니다.",
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true,
            type = "String")
    private String coupleCode;

}