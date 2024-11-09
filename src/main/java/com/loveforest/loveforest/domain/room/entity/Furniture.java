package com.loveforest.loveforest.domain.room.entity;

import jakarta.persistence.*;
import lombok.Builder;
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

    @Column(name = "price", nullable = false)
    private int price; // 가구의 가격

    @Column(name = "width", nullable = false)
    private int width; // 가구의 너비

    @Column(name = "height", nullable = false)
    private int height; // 가구의 높이



    @Builder
    public Furniture(String name, int width, int height, int price) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.price = price;
    }
}