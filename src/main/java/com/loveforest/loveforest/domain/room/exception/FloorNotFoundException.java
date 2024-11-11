package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class FloorNotFoundException extends CustomException {
    public FloorNotFoundException() {
        super(ErrorCode.FLOOR_NOT_FOUND);
    }
}