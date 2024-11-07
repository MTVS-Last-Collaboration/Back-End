package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class FurnitureOverlapException extends CustomException {
    public FurnitureOverlapException() {
        super(ErrorCode.FURNITURE_OVERLAP);
    }
}
