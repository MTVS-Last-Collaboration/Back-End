package com.loveforest.loveforest.domain.shop.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class ItemNotAvailableException extends CustomException {
    public ItemNotAvailableException() {
        super(ErrorCode.ITEM_NOT_AVAILABLE);
    }
}
