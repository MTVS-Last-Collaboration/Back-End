package com.loveforest.loveforest.domain.flower.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FlowerRequestDTO {

    @NotBlank(message = "꽃 이름은 비어 있을 수 없습니다.")
    @Schema(description = "꽃의 새로운 이름", example = "장미")
    private String name;
}
