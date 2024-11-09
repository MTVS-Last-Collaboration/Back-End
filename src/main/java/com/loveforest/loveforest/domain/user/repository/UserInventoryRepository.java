package com.loveforest.loveforest.domain.user.repository;

import com.loveforest.loveforest.domain.user.entity.UserInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserInventoryRepository extends JpaRepository<UserInventory, Long> {
}

