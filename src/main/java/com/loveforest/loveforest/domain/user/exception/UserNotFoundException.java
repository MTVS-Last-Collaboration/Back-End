package com.loveforest.loveforest.domain.user.exception;


import com.loveforest.loveforest.exception.ErrorCode;
import com.loveforest.loveforest.exception.CustomException;

public class UserNotFoundException extends CustomException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}