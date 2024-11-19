package com.loveforest.loveforest.domain.photoAlbum.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class Photo3DConvertFailedException extends CustomException {
    public Photo3DConvertFailedException() {
        super(ErrorCode.PHOTO_3D_CONVERT_FAILED);
    }
}
