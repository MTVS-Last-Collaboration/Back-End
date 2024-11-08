package com.loveforest.loveforest.domain.photoAlbum.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class PhotoNotFoundException extends CustomException {
    public PhotoNotFoundException() {
        super(ErrorCode.PHOTO_NOT_FOUND);
    }
}
