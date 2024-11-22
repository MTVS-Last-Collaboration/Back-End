package com.loveforest.loveforest.domain.flower.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "음성 메시지 상태 정보")
public class VoiceMessageStatusDTO {
    @Schema(description = "파트너 녹음 완료 여부")
    private boolean partnerRecordComplete;

    @Schema(description = "파트너 청취 완료 여부")
    private boolean partnerListenComplete;

    @Schema(description = "파트너 저장 시간")
    private LocalDateTime partnerSavedAt;

    @Schema(description = "파트너 청취 시간")
    private LocalDateTime partnerListenedAt;

    @Schema(description = "파트너 중립 또는 긍정 받은 횟수")
    private int partnerMoodCount;

    @Schema(description = "파트너 꽃 닉네임")
    private String partnerFlowerName;

    @Schema(description = "나의 녹음 완료 여부")
    private boolean myRecordComplete;

    @Schema(description = "나의 청취 완료 여부")
    private boolean myListenComplete;

    @Schema(description = "나의 저장 시간")
    private LocalDateTime mySavedAt;

    @Schema(description = "나의 청취 시간")
    private LocalDateTime myListenedAt;

    @Schema(description = "나의 중립 또는 긍정 받은 횟수")
    private int myMoodCount;

    @Schema(description = "나의 꽃 닉네임")
    private String myFlowerName;
}
