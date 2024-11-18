package com.loveforest.loveforest.domain.flower.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class VoiceMessageNotFoundException extends CustomException {
    public VoiceMessageNotFoundException() {
        super(ErrorCode.VOICE_MESSAGE_NOT_FOUND);
    }
}
