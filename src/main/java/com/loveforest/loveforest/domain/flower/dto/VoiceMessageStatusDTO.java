package com.loveforest.loveforest.domain.flower.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "음성 메시지 상태 정보")
public class VoiceMessageStatusDTO {
    @Schema(description = "녹음 완료 여부")
    private boolean recordComplete;

    @Schema(description = "청취 완료 여부")
    private boolean listenComplete;

    @Schema(description = "저장 시간")
    private LocalDateTime savedAt;

    @Schema(description = "청취 시간")
    private LocalDateTime listenedAt;

    @Schema(description = "중립 또는 긍정 받은 횟수")
    private int moodCount;

    @Schema(description = "꽃 닉네임")
    private String flowerName;
}
