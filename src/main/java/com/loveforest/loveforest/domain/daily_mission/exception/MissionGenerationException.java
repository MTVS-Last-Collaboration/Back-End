package com.loveforest.loveforest.domain.daily_mission.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class MissionGenerationException extends CustomException {
    public MissionGenerationException() {
        super(ErrorCode.MISSION_GENERATION_FAILED);
    }
}