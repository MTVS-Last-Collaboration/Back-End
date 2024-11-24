package com.loveforest.loveforest.domain.room.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_preset_furniture_layout")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PresetFurnitureLayout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "furniture_id", nullable = false)
    private Furniture furniture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preset_room_id", nullable = false)
    private PresetRoom presetRoom;

    @Column(nullable = false)
    private int positionX;

    @Column(nullable = false)
    private int positionY;

    @Column(nullable = false)
    private int rotation;

    @Builder
    public PresetFurnitureLayout(Furniture furniture, int positionX, int positionY, int rotation) {
        this.furniture = furniture;
        this.positionX = positionX;
        this.positionY = positionY;
        this.rotation = rotation;
    }

    void setPresetRoom(PresetRoom presetRoom) {
        this.presetRoom = presetRoom;
    }
}
