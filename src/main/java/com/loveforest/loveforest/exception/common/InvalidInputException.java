package com.loveforest.loveforest.exception.common;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class InvalidInputException extends CustomException {
    public InvalidInputException() {
        super(ErrorCode.INVALID_INPUT);
    }

    // ErrorCode를 직접 지정할 수 있는 생성자 추가
    public InvalidInputException(ErrorCode errorCode) {
        super(errorCode);
    }
}