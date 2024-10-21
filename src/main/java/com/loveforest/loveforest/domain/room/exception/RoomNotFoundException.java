package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class RoomNotFoundException extends CustomException {

    public RoomNotFoundException() {
        super(ErrorCode.ROOM_NOT_FOUND);
    }
}