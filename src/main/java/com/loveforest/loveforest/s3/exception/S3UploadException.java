package com.loveforest.loveforest.s3.exception;

import lombok.Getter;

@Getter
public class S3UploadException extends RuntimeException {
    public S3UploadException(String message) {
        super(message);
    }
}