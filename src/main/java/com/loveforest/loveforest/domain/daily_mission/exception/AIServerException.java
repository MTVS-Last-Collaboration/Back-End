package com.loveforest.loveforest.domain.daily_mission.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class AIServerException extends CustomException {
    public AIServerException() {
        super(ErrorCode.AI_SERVER_ERROR);
    }
}
