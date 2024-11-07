package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class FurnitureNotFoundException extends CustomException {
    public FurnitureNotFoundException() {
        super(ErrorCode.FURNITURE_NOT_FOUND);
    }
}
