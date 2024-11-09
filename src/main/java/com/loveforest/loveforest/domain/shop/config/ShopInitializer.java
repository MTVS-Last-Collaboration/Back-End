package com.loveforest.loveforest.domain.shop.config;

import com.loveforest.loveforest.domain.room.entity.Floor;
import com.loveforest.loveforest.domain.room.entity.Furniture;
import com.loveforest.loveforest.domain.room.entity.Wallpaper;
import com.loveforest.loveforest.domain.room.repository.FloorRepository;
import com.loveforest.loveforest.domain.room.repository.FurnitureRepository;
import com.loveforest.loveforest.domain.room.repository.WallpaperRepository;
import com.loveforest.loveforest.domain.shop.entity.ShopItem;
import com.loveforest.loveforest.domain.shop.exception.InitializeException;
import com.loveforest.loveforest.domain.shop.repository.ShopItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShopInitializer implements CommandLineRunner {

    private final ShopItemRepository shopItemRepository;
    private final FurnitureRepository furnitureRepository;
    private final WallpaperRepository wallpaperRepository;
    private final FloorRepository floorRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            if (shopItemRepository.count() > 0) {
                log.info("상점 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
                return;
            }

            initializeFurniture();
            initializeWallpaper();
            initializeFloor();

            log.info("상점 초기화가 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            log.error("상점 초기화 중 오류 발생: {}", e.getMessage(), e);
            throw new InitializeException("상점 데이터 초기화 실패");
        }
    }

    private void initializeFurniture() {
        log.info("가구 데이터 초기화 시작...");

        List<Furniture> furnitureList = Arrays.asList(
                createFurniture("BedsideTable", 2, 3, 1500),
                createFurniture("BigDrawer", 1, 3, 1500),
                createFurniture("Chair_Blue", 1, 1, 1000),
                createFurniture("Chair_Pink", 1, 1, 1000),
                createFurniture("Chair_White", 1, 1, 1000),
                createFurniture("Chair_Yellow", 1, 1, 1000),
                createFurniture("CornerLight", 1, 1, 500),
                createFurniture("Desk1", 2, 2, 1500),
                createFurniture("DeskPlant", 1, 1, 500),
                createFurniture("DoubleBed_Blue", 4, 4, 2000),
                createFurniture("DoubleBed_Gray", 4, 4, 2000),
                createFurniture("DoubleBed_Pink", 4, 4, 2000),
                createFurniture("DoubleBed_Yellow", 4, 4, 2000),
                createFurniture("Drawer1", 2, 1, 1500),
                createFurniture("Drawer3", 2, 1, 1500),
                createFurniture("Dresser", 3, 1, 1500),
                createFurniture("Fatboy", 2, 2, 1000),
                createFurniture("Fridge", 2, 2, 1500),
                createFurniture("LaunchTable", 2, 3, 1500),
                createFurniture("LoungeChair1", 1, 1, 1000),
                createFurniture("Minifridge", 2, 2, 1500),
                createFurniture("Plant2", 1, 1, 500),
                createFurniture("PlantBox", 1, 2, 500),
                createFurniture("Sofa", 1, 4, 1000),
                createFurniture("Sofa1", 2, 2, 1000),
                createFurniture("Sofa2", 2, 3, 1000),
                createFurniture("Sofa2_Blue", 2, 3, 1000),
                createFurniture("Wardrobe", 1, 4, 1500)
        );

        furnitureRepository.saveAll(furnitureList);

        // ShopItem으로 변환하여 저장
        List<ShopItem> shopItems = furnitureList.stream()
                .map(furniture -> ShopItem.createFurnitureItem(furniture, furniture.getPrice()))
                .toList();

        shopItemRepository.saveAll(shopItems);
        log.info("가구 {}개 초기화 완료", furnitureList.size());
    }

    private void initializeWallpaper() {
        log.info("벽지 데이터 초기화 시작...");

        List<Wallpaper> wallpapers = new ArrayList<>();
        for (int i = 1; i <= 11; i++) {
            wallpapers.add(Wallpaper.builder()
                    .wallpaperNumber(i)
                    .name("Wallpaper " + i)
                    .build());
        }

        wallpaperRepository.saveAll(wallpapers);

        List<ShopItem> shopItems = wallpapers.stream()
                .map(wallpaper -> ShopItem.createWallpaperItem(wallpaper, 500))
                .toList();

        shopItemRepository.saveAll(shopItems);
        log.info("벽지 11개 초기화 완료");
    }

    private void initializeFloor() {
        log.info("바닥 데이터 초기화 시작...");

        List<Floor> floors = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            floors.add(Floor.builder()
                    .floorNumber(i)
                    .name("Floor " + i)
                    .build());
        }

        floorRepository.saveAll(floors);

        List<ShopItem> shopItems = floors.stream()
                .map(floor -> ShopItem.createFloorItem(floor, 500))
                .toList();

        shopItemRepository.saveAll(shopItems);
        log.info("바닥 5개 초기화 완료");
    }

    private Furniture createFurniture(String name, int width, int height, int price) {
        return Furniture.builder()
                .name(name)
                .width(width)          // xSize 대신 width 사용
                .height(height)        // zSize 대신 height 사용
                .price(price)
                .build();
    }
}