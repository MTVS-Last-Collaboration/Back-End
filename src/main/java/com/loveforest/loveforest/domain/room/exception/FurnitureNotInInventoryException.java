package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class FurnitureNotInInventoryException extends CustomException {
    public FurnitureNotInInventoryException() {
        super(ErrorCode.FURNITURE_NOT_IN_INVENTORY);
    }
}