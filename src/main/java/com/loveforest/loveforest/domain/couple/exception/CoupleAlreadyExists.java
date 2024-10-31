package com.loveforest.loveforest.domain.couple.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class CoupleAlreadyExists extends CustomException {
    public CoupleAlreadyExists() {
        super(ErrorCode.COUPLE_ALREADY_EXISTS);
    }
}
