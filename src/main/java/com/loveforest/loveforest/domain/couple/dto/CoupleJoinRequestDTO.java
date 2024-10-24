package com.loveforest.loveforest.domain.couple.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "커플 코드 연동 요청 DTO")
public class CoupleJoinRequestDTO {

    @Schema(description = "연동할 커플 코드. 커플 코드는 첫 번째 사용자가 생성한 고유 코드입니다.",
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true,
            type = "String")
    @NotBlank(message = "커플 코드는 필수 입력 값입니다.")
    private String coupleCode;
}
