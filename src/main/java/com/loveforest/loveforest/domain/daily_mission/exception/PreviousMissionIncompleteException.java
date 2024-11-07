package com.loveforest.loveforest.domain.daily_mission.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class PreviousMissionIncompleteException extends CustomException {
    public PreviousMissionIncompleteException() {
        super(ErrorCode.PREVIOUS_MISSION_INCOMPLETE);
    }
}
