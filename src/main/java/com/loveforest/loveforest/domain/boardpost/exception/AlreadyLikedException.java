package com.loveforest.loveforest.domain.boardpost.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class AlreadyLikedException extends CustomException {
    public AlreadyLikedException() {
        super(ErrorCode.LIKE_ALREADY_EXIST);
    }
}
