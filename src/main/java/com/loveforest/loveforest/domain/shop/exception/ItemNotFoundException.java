package com.loveforest.loveforest.domain.shop.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ItemNotFoundException extends CustomException {
    public ItemNotFoundException() {
        super(ErrorCode.ITEM_NOT_FOUND);  // ErrorCode에 추가 필요
    }
}
