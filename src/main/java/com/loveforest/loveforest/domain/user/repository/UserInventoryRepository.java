package com.loveforest.loveforest.domain.user.repository;

import com.loveforest.loveforest.domain.shop.entity.UserInventory;
import com.loveforest.loveforest.domain.user.entity.UserInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInventoryRepository extends JpaRepository<UserInventory, Long> {
    boolean existsByUserIdAndItemId(Long userId, Long itemId);
    List<UserInventory> findByUserId(Long userId);
}

