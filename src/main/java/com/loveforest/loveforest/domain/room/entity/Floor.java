package com.loveforest.loveforest.domain.room.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_floor")
@Getter
@NoArgsConstructor
public class Floor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int floorNumber;  // 1~5까지의 바닥 번호

    @Column(nullable = false)
    private String name;


    @Builder
    public Floor(int floorNumber, String name) {
        this.floorNumber = floorNumber;
        this.name = name;
    }
}
