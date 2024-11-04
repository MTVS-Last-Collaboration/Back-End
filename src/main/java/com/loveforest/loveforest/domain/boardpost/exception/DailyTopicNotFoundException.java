package com.loveforest.loveforest.domain.boardpost.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class DailyTopicNotFoundException extends CustomException {
    public DailyTopicNotFoundException() {
        super(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
}
