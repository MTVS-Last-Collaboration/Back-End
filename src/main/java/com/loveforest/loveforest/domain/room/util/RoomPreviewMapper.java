package com.loveforest.loveforest.domain.room.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loveforest.loveforest.domain.room.dto.FurnitureLayoutDTO;
import com.loveforest.loveforest.domain.room.dto.RoomPreviewDTO;
import com.loveforest.loveforest.domain.room.dto.RoomStateDTO;
import com.loveforest.loveforest.domain.room.entity.Room;
import com.loveforest.loveforest.domain.room.exception.RoomSerializationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomPreviewMapper {

    public RoomPreviewDTO createFromRoom(Room room) {
        return RoomPreviewDTO.builder()
                .wallpaperName(room.getWallpaper() != null ? room.getWallpaper().getName() : null)
                .floorName(room.getFloor() != null ? room.getFloor().getName() : null)
                .furnitureNames(room.getFurnitureLayouts().stream()
                        .map(layout -> layout.getFurniture().getName())
                        .collect(Collectors.toList()))
                .totalFurniture(room.getFurnitureLayouts().size())
                .build();
    }

    public RoomPreviewDTO createFromJson(String roomData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            RoomStateDTO state = mapper.readValue(roomData, RoomStateDTO.class);

            return RoomPreviewDTO.builder()
                    .wallpaperName(state.getWallpaper() != null ?
                            state.getWallpaper().getName() : null)
                    .floorName(state.getFloor() != null ?
                            state.getFloor().getName() : null)
                    .furnitureNames(state.getFurnitureLayouts().stream()
                            .map(FurnitureLayoutDTO::getName)
                            .collect(Collectors.toList()))
                    .totalFurniture(state.getFurnitureLayouts().size())
                    .build();
        } catch (JsonProcessingException e) {
            log.error("방 미리보기 생성 실패", e);
            throw new RoomSerializationException("방 미리보기 생성 실패", e);
        }
    }
}