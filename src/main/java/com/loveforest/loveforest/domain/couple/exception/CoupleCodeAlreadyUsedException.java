package com.loveforest.loveforest.domain.couple.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class CoupleCodeAlreadyUsedException extends CustomException {
    public CoupleCodeAlreadyUsedException() {
        super(ErrorCode.COUPLE_CODE_ALREADY_USED);
    }
}