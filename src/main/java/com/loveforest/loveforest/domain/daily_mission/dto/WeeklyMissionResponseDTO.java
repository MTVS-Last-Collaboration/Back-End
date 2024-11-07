package com.loveforest.loveforest.domain.daily_mission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyMissionResponseDTO {
    private List<DailyMissionContent> missions;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyMissionContent {
        private LocalDate date;
        private String content;
    }
}