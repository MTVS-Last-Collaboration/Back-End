package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class RoomImageUploadException extends CustomException {
    public RoomImageUploadException() {
        super(ErrorCode.ROOM_IMAGE_UPLOAD_FAILED);
    }
}
