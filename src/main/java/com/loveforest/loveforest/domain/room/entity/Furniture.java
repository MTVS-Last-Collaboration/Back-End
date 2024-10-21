package com.loveforest.loveforest.domain.room.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_furniture")
@Getter
@NoArgsConstructor
public class Furniture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type; // 가구 유형 (의자, 테이블 등)

    @Column(name = "price", nullable = false)
    private int price; // 가구의 가격

    // 생성자
    public Furniture(String name, String type, int price) {
        this.name = name;
        this.type = type;
        this.price = price;
    }
}