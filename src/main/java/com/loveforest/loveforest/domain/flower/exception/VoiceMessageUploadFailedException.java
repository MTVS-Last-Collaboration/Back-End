package com.loveforest.loveforest.domain.flower.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class VoiceMessageUploadFailedException extends CustomException {
    public VoiceMessageUploadFailedException() {
        super(ErrorCode.VOICE_MESSAGE_UPLOAD_FAILED);
    }
}
