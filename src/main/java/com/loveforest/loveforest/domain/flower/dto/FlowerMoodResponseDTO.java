package com.loveforest.loveforest.domain.flower.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FlowerMoodResponseDTO {

    @Schema(description = "분석된 기분 상태 (상/중/하 중 하나)", example = "상")
    private String mood;

}
