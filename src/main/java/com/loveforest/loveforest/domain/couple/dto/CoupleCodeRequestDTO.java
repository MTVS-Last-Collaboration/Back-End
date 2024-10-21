package com.loveforest.loveforest.domain.couple.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoupleCodeRequestDTO {

    @Schema(description = "커플 코드 생성에 필요한 사용자 ID",
            example = "1",
            required = true,
            type = "Long")
    @NotNull(message = "사용자 ID는 필수 입력 값입니다.")
    private Long userId;

}