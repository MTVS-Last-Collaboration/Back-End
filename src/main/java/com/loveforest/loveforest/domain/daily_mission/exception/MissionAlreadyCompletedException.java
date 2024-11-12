package com.loveforest.loveforest.domain.daily_mission.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class MissionAlreadyCompletedException extends CustomException {
    public MissionAlreadyCompletedException() {
        super(ErrorCode.MISSION_ALREADY_COMPLETED); // ErrorCode에 추가 필요
    }
}