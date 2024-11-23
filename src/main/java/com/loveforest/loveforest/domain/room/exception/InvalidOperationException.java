package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class InvalidOperationException extends CustomException {
    public InvalidOperationException() {
        super(ErrorCode.INVALID_OPERATION);
    }
}