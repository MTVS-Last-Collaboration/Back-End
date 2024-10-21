package com.loveforest.loveforest.domain.couple.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

public class CoupleNotFoundException extends CustomException {
    public CoupleNotFoundException() {
        super(ErrorCode.COUPLE_NOT_FOUND);
    }
}
