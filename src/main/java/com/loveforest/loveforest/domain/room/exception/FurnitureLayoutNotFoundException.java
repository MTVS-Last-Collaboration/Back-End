package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class FurnitureLayoutNotFoundException extends CustomException {
    public FurnitureLayoutNotFoundException() {
        super(ErrorCode.FURNITURE_LAYOUT_NOT_FOUND);
    }
}
