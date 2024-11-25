package com.loveforest.loveforest.domain.room.dto;

import com.loveforest.loveforest.domain.room.entity.Floor;
import com.loveforest.loveforest.domain.room.entity.Wallpaper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomStateDTO {
    private Long wallpaperId;
    private Long floorId;
    private List<FurnitureLayoutDTO> furnitureLayouts;

    public void setFurnitureLayouts(List<FurnitureLayoutDTO> furnitureLayouts) {
        this.furnitureLayouts = furnitureLayouts;
    }
}