package com.loveforest.loveforest.domain.photoAlbum.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class ModelNotReadyException extends CustomException {
    public ModelNotReadyException(String message) {
        super(ErrorCode.MODEL_NOT_READY);
    }
}
