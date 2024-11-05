package com.loveforest.loveforest.domain.boardpost.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class CommentsNotFoundException extends CustomException {
    public CommentsNotFoundException() {
        super(ErrorCode.COMMENT_NOT_FOUND);
    }
}
