package com.loveforest.loveforest.domain.shop.service;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.exception.CoupleNotFoundException;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.room.entity.Floor;
import com.loveforest.loveforest.domain.room.entity.Furniture;
import com.loveforest.loveforest.domain.room.entity.Wallpaper;
import com.loveforest.loveforest.domain.shop.config.ShopValidator;
import com.loveforest.loveforest.domain.shop.dto.PurchaseResponseDTO;
import com.loveforest.loveforest.domain.shop.dto.ShopItemDTO;
import com.loveforest.loveforest.domain.shop.entity.ItemType;
import com.loveforest.loveforest.domain.shop.entity.PurchaseHistory;
import com.loveforest.loveforest.domain.shop.entity.ShopItem;
import com.loveforest.loveforest.domain.shop.exception.ItemNotFoundException;
import com.loveforest.loveforest.domain.shop.exception.PurchaseProcessingException;
import com.loveforest.loveforest.domain.shop.repository.PurchaseHistoryRepository;
import com.loveforest.loveforest.domain.shop.repository.ShopItemRepository;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.entity.UserInventory;
import com.loveforest.loveforest.domain.user.exception.UserNotFoundException;
import com.loveforest.loveforest.domain.user.repository.UserInventoryRepository;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopService {
    private final ShopItemRepository shopItemRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final ShopValidator shopValidator;
    private final UserInventoryRepository userInventoryRepository;

    public List<ShopItemDTO> getItemsByType(ItemType itemType) {
        return shopItemRepository.findByItemTypeAndAvailableTrue(itemType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PurchaseResponseDTO purchaseItem(Long itemId, Long userId, Long coupleId, int currentPoints) {
        // 1. 엔티티 조회
        ShopItem shopItem = findShopItemById(itemId);
        User user = findUserById(userId);
        Couple couple = findCoupleById(coupleId);

        // 2. 구매 가능 여부 검증
        shopValidator.validatePurchase(shopItem, couple, currentPoints);

        try {
            // 3. 포인트 차감 및 구매 처리
            processPurchase(shopItem, user, couple);

            // 4. 구매 이력 저장
            PurchaseHistory purchaseHistory = savePurchaseHistory(user, shopItem);

            // 5. UserInventory에 아이템 추가
            addItemToUserInventory(user, shopItem);

            log.info("아이템 구매 성공 - userId: {}, itemId: {}, itemType: {}",
                    userId, itemId, shopItem.getItemType());

            return createPurchaseResponse(purchaseHistory);
        } catch (Exception e) {
            log.error("구매 처리 중 오류 발생", e);
            throw new PurchaseProcessingException();
        }
    }

    private ShopItem findShopItemById(Long itemId) {
        return shopItemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("상품을 찾을 수 없음 - itemId: {}", itemId);
                    return new ItemNotFoundException();
                });
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음 - userId: {}", userId);
                    return new UserNotFoundException();
                });
    }

    private Couple findCoupleById(Long coupleId) {
        return coupleRepository.findById(coupleId)
                .orElseThrow(() -> {
                    log.error("커플을 찾을 수 없음 - coupleId: {}", coupleId);
                    return new CoupleNotFoundException();
                });
    }

    private void processPurchase(ShopItem shopItem, User user, Couple couple) {
        couple.deductPoints(shopItem.getPrice());
        coupleRepository.save(couple);
    }

    private PurchaseHistory savePurchaseHistory(User user, ShopItem shopItem) {
        PurchaseHistory purchaseHistory = PurchaseHistory.builder()
                .user(user)
                .shopItem(shopItem)
                .pricePaid(shopItem.getPrice())
                .build();
        return purchaseHistoryRepository.save(purchaseHistory);
    }

    private void addItemToUserInventory(User user, ShopItem shopItem) {
        UserInventory userInventory = new UserInventory(user, shopItem);
        userInventoryRepository.save(userInventory);
    }

    private PurchaseResponseDTO createPurchaseResponse(PurchaseHistory purchaseHistory) {
        return PurchaseResponseDTO.builder()
                .purchaseId(purchaseHistory.getId())
                .itemName(purchaseHistory.getShopItem().getName())
                .pricePaid(purchaseHistory.getPricePaid())
                .purchasedAt(purchaseHistory.getPurchasedAt())
                .build();
    }

    private ShopItemDTO convertToDTO(ShopItem shopItem) {
        ShopItemDTO.ShopItemDTOBuilder builder = ShopItemDTO.builder()
                .id(shopItem.getId())
                .itemType(shopItem.getItemType())
                .name(shopItem.getName())
                .price(shopItem.getPrice());

        addItemSpecificDetails(builder, shopItem);

        return builder.build();
    }

    private void addItemSpecificDetails(ShopItemDTO.ShopItemDTOBuilder builder, ShopItem shopItem) {
        switch (shopItem.getItemType()) {
            case FURNITURE -> addFurnitureDetails(builder, shopItem.getFurniture());
            case WALLPAPER -> addWallpaperDetails(builder, shopItem.getWallpaper());
            case FLOOR -> addFloorDetails(builder, shopItem.getFloor());
        }
    }

    private void addFurnitureDetails(ShopItemDTO.ShopItemDTOBuilder builder, Furniture furniture) {
        if (furniture != null) {
            builder.width(furniture.getWidth())
                    .height(furniture.getHeight());
        }
    }

    private void addWallpaperDetails(ShopItemDTO.ShopItemDTOBuilder builder, Wallpaper wallpaper) {
        if (wallpaper != null) {
            builder.number(wallpaper.getWallpaperNumber())
                    .name(wallpaper.getName());
        }
    }

    private void addFloorDetails(ShopItemDTO.ShopItemDTOBuilder builder, Floor floor) {
        if (floor != null) {
            builder.number(floor.getFloorNumber())
                    .name(floor.getName());
        }
    }
}