package com.loveforest.loveforest.domain.daily_mission.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class MissionAlreadyAnsweredException extends CustomException {
    public MissionAlreadyAnsweredException() {
        super(ErrorCode.MISSION_ALREADY_ANSWERED);
    }
}