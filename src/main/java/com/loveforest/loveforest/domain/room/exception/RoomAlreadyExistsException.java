package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class RoomAlreadyExistsException extends CustomException {
    public RoomAlreadyExistsException() {
        super(ErrorCode.ROOM_ALREADY_EXISTS);
    }
}

