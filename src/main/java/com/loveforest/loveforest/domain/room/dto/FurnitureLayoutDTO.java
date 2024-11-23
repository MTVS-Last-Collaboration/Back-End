package com.loveforest.loveforest.domain.room.dto;

import com.loveforest.loveforest.domain.room.entity.Furniture;
import com.loveforest.loveforest.domain.room.entity.FurnitureLayout;
import com.loveforest.loveforest.domain.room.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FurnitureLayoutDTO {
    private Long furnitureId;
    private String name;
    private int positionX;
    private int positionY;
    private int rotation;

    public static FurnitureLayoutDTO from(FurnitureLayout layout) {
        return FurnitureLayoutDTO.builder()
                .furnitureId(layout.getFurniture().getId())
                .name(layout.getFurniture().getName())
                .positionX(layout.getPositionX())
                .positionY(layout.getPositionY())
                .rotation(layout.getRotation())
                .build();
    }

    public FurnitureLayout toEntity(Room room, Furniture furniture) {
        return FurnitureLayout.builder()
                .room(room)
                .furniture(furniture)
                .positionX(this.positionX)
                .positionY(this.positionY)
                .rotation(this.rotation)
                .build();
    }
}