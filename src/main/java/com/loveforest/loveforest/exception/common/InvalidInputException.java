package com.loveforest.loveforest.exception.common;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class InvalidInputException extends CustomException {
    public InvalidInputException() {
        super(ErrorCode.INVALID_INPUT);
    }
}