package com.loveforest.loveforest.domain.photoAlbum.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class DuplicatePhotoPositionException extends CustomException {
    public DuplicatePhotoPositionException() {
        super(ErrorCode.DUPLICATE_PHOTO_POSITION);
    }
}