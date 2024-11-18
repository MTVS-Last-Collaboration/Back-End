package com.loveforest.loveforest.domain.flower.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VoiceAnalysisRequestDTO {
    @NotNull(message = "음성 데이터는 필수입니다")
    @Size(min = 1, message = "음성 데이터가 비어있습니다")
    @Schema(description = "base64 인코딩된 음성메세지", example = "base64EncodedVoiceMessage")
    private String voiceData;

}
