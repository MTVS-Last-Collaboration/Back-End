package com.loveforest.loveforest.domain.daily_mission.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class DailyMissionAlreadyExistsException extends CustomException {
    public DailyMissionAlreadyExistsException() {
        super(ErrorCode.DAILY_MISSION_ALREADY_EXISTS);
    }
}