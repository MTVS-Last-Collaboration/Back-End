package com.loveforest.loveforest.domain.photoAlbum.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class PhotoUploadFailedException extends CustomException {
    public PhotoUploadFailedException() {
        super(ErrorCode.PHOTO_UPLOAD_FAILED);
    }
}
