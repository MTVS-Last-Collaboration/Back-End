package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class InvalidRoomCreationException extends CustomException {
    public InvalidRoomCreationException() {
        super(ErrorCode.INVALID_ROOM_CREATION);
    }
}
