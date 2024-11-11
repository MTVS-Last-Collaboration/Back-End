package com.loveforest.loveforest.domain.room.exception;

import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;

public class WallpaperNotInInventoryException extends CustomException {
    public WallpaperNotInInventoryException() {
        super(ErrorCode.WALLPAPER_NOT_IN_INVENTORY);
    }
}