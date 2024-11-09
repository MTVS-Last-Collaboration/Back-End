package com.loveforest.loveforest.domain.shop.entity;

public enum ItemType {
    FURNITURE("가구"),
    WALLPAPER("벽지"),
    FLOOR("바닥");

    private final String description;

    ItemType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
