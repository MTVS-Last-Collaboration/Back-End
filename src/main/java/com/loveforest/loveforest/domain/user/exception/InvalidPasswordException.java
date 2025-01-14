package com.loveforest.loveforest.domain.user.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidPasswordException extends CustomException {
    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD);
    }
}