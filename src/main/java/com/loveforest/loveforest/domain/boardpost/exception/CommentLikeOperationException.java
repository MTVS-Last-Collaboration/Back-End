package com.loveforest.loveforest.domain.boardpost.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class CommentLikeOperationException extends CustomException {
    public CommentLikeOperationException() {
        super(ErrorCode.COMMENT_LIKE_OPERATION_FAILED);
    }
}