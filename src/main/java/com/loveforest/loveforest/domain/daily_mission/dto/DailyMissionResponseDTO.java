package com.loveforest.loveforest.domain.daily_mission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Schema(description = "데일리 미션 응답 DTO")
@Getter
@AllArgsConstructor
public class DailyMissionResponseDTO {
    @Schema(description = "미션 번호", example = "1")
    private Integer missionNumber;

    @Schema(description = "미션 날짜", example = "2024-11-08")
    private LocalDate missionDate;

    @Schema(description = "미션 내용", example = "오늘 하루 동안 가장 행복했던 순간은?")
    private String missionContent;

    @Schema(description = "첫 번째 파트너의 기분", example = "행복")
    private String partner1Mood;

    @Schema(description = "첫 번째 파트너의 답변", example = "함께 산책할 때입니다.")
    private String partner1Answer;

    @Schema(description = "두 번째 파트너의 기분", example = "기쁨")
    private String partner2Mood;

    @Schema(description = "두 번째 파트너의 답변", example = "깜짝 선물을 받았을 때요.")
    private String partner2Answer;

    @Schema(description = "미션 완료 여부", example = "true")
    private boolean isCompleted;
}
