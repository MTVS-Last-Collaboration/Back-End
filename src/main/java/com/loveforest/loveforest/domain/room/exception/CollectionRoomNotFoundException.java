package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class CollectionRoomNotFoundException extends CustomException {
    public CollectionRoomNotFoundException() {
        super(ErrorCode.COLLECTION_ROOM_NOT_FOUND);
    }
}