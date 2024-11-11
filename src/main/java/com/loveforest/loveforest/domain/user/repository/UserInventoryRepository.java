package com.loveforest.loveforest.domain.user.repository;

import com.loveforest.loveforest.domain.user.entity.UserInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserInventoryRepository extends JpaRepository<UserInventory, Long> {

    // 커플이 특정 가구를 보유하고 있는지 확인
    boolean existsByUser_Couple_IdAndShopItem_Furniture_Id(Long coupleId, Long furnitureId);

    // 커플의 모든 가구 목록 조회
    @Query("SELECT ui FROM UserInventory ui " +
            "WHERE ui.user.couple.id = :coupleId " +
            "AND ui.shopItem.furniture IS NOT NULL")
    List<UserInventory> findAllFurnitureByCouple(@Param("coupleId") Long coupleId);
}

