package com.loveforest.loveforest.domain.shop.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class InsufficientPointsException extends CustomException {
    public InsufficientPointsException() {
        super(ErrorCode.INSUFFICIENT_POINTS);  // ErrorCode에 추가 필요
    }
}
