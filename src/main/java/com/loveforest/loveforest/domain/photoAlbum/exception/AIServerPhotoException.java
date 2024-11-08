package com.loveforest.loveforest.domain.photoAlbum.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class AIServerPhotoException extends CustomException {
    public AIServerPhotoException() {
        super(ErrorCode.AI_SERVER_ERROR_PHOTO);
    }
}
