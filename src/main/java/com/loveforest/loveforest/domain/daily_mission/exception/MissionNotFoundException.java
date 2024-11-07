package com.loveforest.loveforest.domain.daily_mission.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class MissionNotFoundException extends CustomException {
    public MissionNotFoundException() {
        super(ErrorCode.MISSION_NOT_FOUND);
    }
}
