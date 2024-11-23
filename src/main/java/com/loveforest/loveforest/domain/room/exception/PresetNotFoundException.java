package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class PresetNotFoundException extends CustomException {
    public PresetNotFoundException() {
        super(ErrorCode.PRESET_NOT_FOUND);
    }
}
