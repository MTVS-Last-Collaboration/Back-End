package com.loveforest.loveforest.domain.shop.entity;

import com.loveforest.loveforest.domain.room.entity.Floor;
import com.loveforest.loveforest.domain.room.entity.Furniture;
import com.loveforest.loveforest.domain.room.entity.Wallpaper;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_shop_item")
@Getter
@NoArgsConstructor
public class ShopItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;

    @OneToOne
    @JoinColumn(name = "furniture_id")
    private Furniture furniture;

    @OneToOne
    @JoinColumn(name = "wallpaper_id")
    private Wallpaper wallpaper;

    @OneToOne
    @JoinColumn(name = "floor_id")
    private Floor floor;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private boolean available = true;

    @Builder
    private ShopItem(ItemType itemType, Furniture furniture, Wallpaper wallpaper,
                     Floor floor, String name, int price) {
        this.itemType = itemType;
        this.furniture = furniture;
        this.wallpaper = wallpaper;
        this.floor = floor;
        this.name = name;
        this.price = price;
    }

    public static ShopItem createFurnitureItem(Furniture furniture, int price) {
        return ShopItem.builder()
                .itemType(ItemType.FURNITURE)
                .furniture(furniture)
                .name(furniture.getName())
                .price(price)
                .build();
    }

    public static ShopItem createWallpaperItem(Wallpaper wallpaper, int price) {
        return ShopItem.builder()
                .itemType(ItemType.WALLPAPER)
                .wallpaper(wallpaper)
                .name(wallpaper.getName())
                .price(price)
                .build();
    }

    public static ShopItem createFloorItem(Floor floor, int price) {
        return ShopItem.builder()
                .itemType(ItemType.FLOOR)
                .floor(floor)
                .name(floor.getName())
                .price(price)
                .build();
    }
}
