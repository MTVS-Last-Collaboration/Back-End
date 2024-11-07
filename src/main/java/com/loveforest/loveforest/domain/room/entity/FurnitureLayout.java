package com.loveforest.loveforest.domain.room.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_furniture_layout")
@Getter
@NoArgsConstructor
public class FurnitureLayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "furniture_id", nullable = false)
    private Furniture furniture; // 배치된 가구

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room; // 가구가 배치된 방

    @Column(name = "position_x", nullable = false)
    private int positionX; // X 좌표

    @Column(name = "position_y", nullable = false)
    private int positionY; // Y 좌표

    @Column(name = "rotation", nullable = false)
    private int rotation; // 가구의 회전 각도

    // 생성자
    public FurnitureLayout(Furniture furniture, int positionX, int positionY, int rotation) {
        this.furniture = furniture;
        this.positionX = positionX;
        this.positionY = positionY;
        this.rotation = rotation;
    }

    // 방 설정 메서드
    public void setRoom(Room room) {
        this.room = room;
    }

    // 위치와 회전 각도 설정 메서드 추가
    public void setPosition(int positionX, int positionY, int rotation) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.rotation = rotation;
    }
}