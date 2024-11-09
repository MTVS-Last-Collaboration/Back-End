package com.loveforest.loveforest.domain.shop.config;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.shop.entity.ShopItem;
import com.loveforest.loveforest.domain.shop.exception.InsufficientPointsException;
import com.loveforest.loveforest.domain.shop.exception.ItemNotAvailableException;
import com.loveforest.loveforest.domain.user.repository.UserInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShopValidator {
    private final UserInventoryRepository userInventoryRepository;

    public void validatePurchase(ShopItem shopItem, int currentPoints) {
        validateItemAvailability(shopItem);
        validatePoints(currentPoints, shopItem.getPrice());
    }

    private void validateItemAvailability(ShopItem shopItem) {
        if (!shopItem.isAvailable()) {
            throw new ItemNotAvailableException();
        }
    }

    private void validatePoints(int currentPoints, int price) {
        if (currentPoints < price) {
            log.warn("포인트 부족 - required: {}, current: {}", price, currentPoints);
            throw new InsufficientPointsException();
        }
    }
}
