package com.loveforest.loveforest.domain.shop.repository;

import com.loveforest.loveforest.domain.shop.entity.PurchaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {
    /**
     * 특정 사용자의 구매 이력을 최근 순으로 조회
     */
    List<PurchaseHistory> findByUserIdOrderByPurchasedAtDesc(Long userId);

    /**
     * 특정 사용자가 특정 아이템을 구매했는지 확인
     */
    boolean existsByUserIdAndShopItemId(Long userId, Long shopItemId);

    /**
     * 특정 기간 동안의 구매 이력 조회
     */
    @Query("SELECT ph FROM PurchaseHistory ph WHERE ph.user.id = :userId " +
            "AND ph.purchasedAt BETWEEN :startDate AND :endDate")
    List<PurchaseHistory> findByUserIdAndPurchaseDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
