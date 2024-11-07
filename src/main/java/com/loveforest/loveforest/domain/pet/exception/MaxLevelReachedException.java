package com.loveforest.loveforest.domain.pet.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class MaxLevelReachedException extends CustomException {
    public MaxLevelReachedException() {
        super(ErrorCode.MAX_LEVEL_REACHED);
    }
}
