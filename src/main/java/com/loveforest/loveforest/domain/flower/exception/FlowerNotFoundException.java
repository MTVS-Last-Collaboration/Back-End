package com.loveforest.loveforest.domain.flower.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class FlowerNotFoundException extends CustomException {
    public FlowerNotFoundException() {
        super(ErrorCode.FLOWER_NOT_FOUND);
    }
}
