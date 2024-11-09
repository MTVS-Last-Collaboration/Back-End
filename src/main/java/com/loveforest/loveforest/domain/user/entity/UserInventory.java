package com.loveforest.loveforest.domain.user.entity;

import com.loveforest.loveforest.domain.shop.entity.ShopItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_user_inventory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInventory {
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
    private LocalDateTime acquiredAt;

    public UserInventory(User user, ShopItem shopItem) {
        this.user = user;
        this.shopItem = shopItem;
        this.acquiredAt = LocalDateTime.now();
    }
}