package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class RoomSerializationException extends CustomException {
    public RoomSerializationException(String message, Throwable cause) {
        super(ErrorCode.ROOM_SERIALIZATION_ERROR);
    }
}
