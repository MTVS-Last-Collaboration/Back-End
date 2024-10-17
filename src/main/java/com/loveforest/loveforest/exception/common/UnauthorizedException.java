package com.loveforest.loveforest.exception.common;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class UnauthorizedException extends CustomException {
    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }
}
