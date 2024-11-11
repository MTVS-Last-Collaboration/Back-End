package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class FloorNotInInventoryException extends CustomException {
    public FloorNotInInventoryException() {
        super(ErrorCode.FLOOR_NOT_IN_INVENTORY);
    }
}