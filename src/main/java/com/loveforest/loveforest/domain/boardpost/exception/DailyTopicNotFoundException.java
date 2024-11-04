package com.loveforest.loveforest.domain.boardpost.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class DailyTopicNotFoundException extends CustomException {
    public DailyTopicNotFoundException() {
        super(ErrorCode.DAILY_TOPIC_NOT_FOUND);
    }
}
