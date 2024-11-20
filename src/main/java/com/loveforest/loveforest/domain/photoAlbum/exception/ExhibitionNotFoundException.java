package com.loveforest.loveforest.domain.photoAlbum.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class ExhibitionNotFoundException extends CustomException {
    public ExhibitionNotFoundException() {
        super(ErrorCode.EXHIBITION_NOT_FOUND);
    }
}
