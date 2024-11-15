package com.loveforest.loveforest.domain.flower.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FlowerMoodResponseDTO {

    @Schema(description = "분석된 기분 상태 (긍정/중립/부정 중 하나)", example = "긍정")
    private String mood;

    @Schema(description = "사용자의 닉네임", example = "송민제")
    private String nickname;

}
