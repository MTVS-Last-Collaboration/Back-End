package com.loveforest.loveforest.domain.pet.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import lombok.Getter;

@Getter
public class PetNotFoundException extends CustomException {
    public PetNotFoundException() {
        super(ErrorCode.PET_NOT_FOUND);
    }
}
