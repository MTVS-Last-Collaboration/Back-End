package com.loveforest.loveforest.domain.shop.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class InitializeException extends CustomException {
    public InitializeException(String message) {
        super(ErrorCode.INITIALIZATION_FAILED);
    }
}