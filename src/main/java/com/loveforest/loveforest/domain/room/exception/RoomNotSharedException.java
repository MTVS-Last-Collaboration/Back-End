package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class RoomNotSharedException extends CustomException {
    public RoomNotSharedException() {
        super(ErrorCode.ROOM_NOT_SHARED);
    }
}

