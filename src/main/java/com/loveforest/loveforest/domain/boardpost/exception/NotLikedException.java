package com.loveforest.loveforest.domain.boardpost.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class NotLikedException extends CustomException {
    public NotLikedException() {
        super(ErrorCode.LIKE_NOT_FOUND);
    }
}
