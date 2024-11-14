package com.loveforest.loveforest.s3.exception;

import lombok.Getter;

@Getter
public class S3AccessException extends RuntimeException {
    public S3AccessException(String message) {
        super(message);
    }
}