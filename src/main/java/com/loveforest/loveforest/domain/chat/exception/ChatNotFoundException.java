package com.loveforest.loveforest.domain.chat.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class ChatNotFoundException extends CustomException {
    public ChatNotFoundException() {
        super(ErrorCode.CHAT_NOT_FOUND);
    }
}