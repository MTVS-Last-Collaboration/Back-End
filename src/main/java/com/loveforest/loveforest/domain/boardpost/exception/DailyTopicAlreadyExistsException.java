package com.loveforest.loveforest.domain.boardpost.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class DailyTopicAlreadyExistsException extends CustomException {
    public DailyTopicAlreadyExistsException() {
        super(ErrorCode.DAILY_TOPIC_ALREADY_EXIST);
    }
}
