package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class RoomCreationFailedException extends CustomException {
    public RoomCreationFailedException() {
        super(ErrorCode.ROOM_CREATION_FAILED);
    }
}