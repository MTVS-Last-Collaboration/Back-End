package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class PresetSaveFailedException extends CustomException {
    public PresetSaveFailedException() {
        super(ErrorCode.PRESET_SAVE_FAILED);
    }
}
