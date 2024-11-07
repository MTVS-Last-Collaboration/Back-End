package com.loveforest.loveforest.domain.flower.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class MaxMoodCountReachedException extends CustomException {

    public MaxMoodCountReachedException() {
        super(ErrorCode.MAX_MOOD_COUNT_REACHED);  // ErrorCode는 아래와 같이 정의해야 합니다.
    }
}
