package com.loveforest.loveforest.domain.auth.jwt.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class InvalidAccessTokenException extends CustomException {

    public InvalidAccessTokenException() {
        super(ErrorCode.ACCESS_TOKEN_EXPIRED);
    }
}
