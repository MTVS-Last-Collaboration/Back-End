package com.loveforest.loveforest.domain.flower.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class AiServerFlowerException extends CustomException {
    public AiServerFlowerException() {
        super(ErrorCode.AI_SERVER_ERROR_FLOWER); // ErrorCode에 AI_SERVER_ERROR 추가 필요
    }
}
