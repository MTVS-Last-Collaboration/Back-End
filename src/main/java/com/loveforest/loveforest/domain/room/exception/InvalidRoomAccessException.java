package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class InvalidRoomAccessException extends CustomException {
    public InvalidRoomAccessException(String message) {
        super(ErrorCode.INVALID_ROOM_ACCESS);
    }
}
