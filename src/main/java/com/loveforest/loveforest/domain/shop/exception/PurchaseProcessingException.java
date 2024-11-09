package com.loveforest.loveforest.domain.shop.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class PurchaseProcessingException extends CustomException {
    public PurchaseProcessingException() {
        super(ErrorCode.PURCHASE_PROCESSING_FAILED);
    }
}
