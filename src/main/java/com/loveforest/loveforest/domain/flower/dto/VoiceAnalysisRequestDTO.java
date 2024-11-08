package com.loveforest.loveforest.domain.flower.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VoiceAnalysisRequestDTO {
    @NotNull(message = "음성 데이터는 필수입니다")
    @Size(min = 1, message = "음성 데이터가 비어있습니다")
    private String voiceData;

}
