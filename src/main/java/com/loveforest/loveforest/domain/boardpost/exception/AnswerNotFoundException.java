package com.loveforest.loveforest.domain.boardpost.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class AnswerNotFoundException extends CustomException {
    public AnswerNotFoundException() {
        super(ErrorCode.ANSWER_NOT_FOUND);
    }
}
