package com.loveforest.loveforest.domain.daily_mission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class WeeklyMissionRequestDTO {
    private LocalDate startDate;  // 미션 시작 날짜
}