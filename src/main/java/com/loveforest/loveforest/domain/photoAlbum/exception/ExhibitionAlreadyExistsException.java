package com.loveforest.loveforest.domain.photoAlbum.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class ExhibitionAlreadyExistsException extends CustomException {
    public ExhibitionAlreadyExistsException() {
        super(ErrorCode.EXHIBITION_ALREADY_EXISTS);
    }
}
