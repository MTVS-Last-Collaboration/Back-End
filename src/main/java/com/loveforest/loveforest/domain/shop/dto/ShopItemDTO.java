package com.loveforest.loveforest.domain.shop.dto;

import com.loveforest.loveforest.domain.shop.entity.ItemType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShopItemDTO {
    private Long id;
    private ItemType itemType;
    private String name;
    private int price;

    // Furniture 전용 필드
    private Integer width;
    private Integer height;

    // Wallpaper, Floor 공통 필드
    private Integer number;  // wallpaperNumber 또는 floorNumber
}