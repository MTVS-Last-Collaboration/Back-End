package com.loveforest.loveforest.domain.shop.repository;

import com.loveforest.loveforest.domain.shop.entity.ItemType;
import com.loveforest.loveforest.domain.shop.entity.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {
    List<ShopItem> findByItemTypeAndAvailableTrue(ItemType itemType);

    @Query("SELECT si FROM ShopItem si WHERE si.available = true")
    List<ShopItem> findAllAvailableItems();
}
