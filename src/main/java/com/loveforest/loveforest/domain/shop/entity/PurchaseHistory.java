package com.loveforest.loveforest.domain.shop.entity;

import com.loveforest.loveforest.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_purchase_history")
@Getter
@NoArgsConstructor
public class PurchaseHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_item_id", nullable = false)
    private ShopItem shopItem;

    @Column(nullable = false)
    private int pricePaid;

    @Column(nullable = false)
    private LocalDateTime purchasedAt;

    @Builder
    public PurchaseHistory(User user, ShopItem shopItem, int pricePaid) {
        this.user = user;
        this.shopItem = shopItem;
        this.pricePaid = pricePaid;
        this.purchasedAt = LocalDateTime.now();
    }
}
