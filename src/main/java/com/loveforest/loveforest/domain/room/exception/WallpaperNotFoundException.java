package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class WallpaperNotFoundException extends CustomException {
    public WallpaperNotFoundException() {
        super(ErrorCode.WALLPAPER_NOT_FOUND);
    }
}