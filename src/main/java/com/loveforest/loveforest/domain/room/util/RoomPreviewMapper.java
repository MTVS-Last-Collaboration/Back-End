package com.loveforest.loveforest.domain.room.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loveforest.loveforest.domain.room.dto.FurnitureLayoutDTO;
import com.loveforest.loveforest.domain.room.dto.RoomPreviewDTO;
import com.loveforest.loveforest.domain.room.dto.RoomStateDTO;
import com.loveforest.loveforest.domain.room.entity.Floor;
import com.loveforest.loveforest.domain.room.entity.Furniture;
import com.loveforest.loveforest.domain.room.entity.Room;
import com.loveforest.loveforest.domain.room.entity.Wallpaper;
import com.loveforest.loveforest.domain.room.exception.RoomSerializationException;
import com.loveforest.loveforest.domain.room.repository.FloorRepository;
import com.loveforest.loveforest.domain.room.repository.FurnitureRepository;
import com.loveforest.loveforest.domain.room.repository.WallpaperRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomPreviewMapper {

    private final WallpaperRepository wallpaperRepository;
    private final FloorRepository floorRepository;
    private final FurnitureRepository furnitureRepository;

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

            // 벽지 이름 조회
            String wallpaperName = null;
            if (state.getWallpaperId() != null) {
                wallpaperName = wallpaperRepository.findById(state.getWallpaperId())
                        .map(Wallpaper::getName)
                        .orElse(null);
            }

            // 바닥 이름 조회
            String floorName = null;
            if (state.getFloorId() != null) {
                floorName = floorRepository.findById(state.getFloorId())
                        .map(Floor::getName)
                        .orElse(null);
            }

            // 가구 이름 조회
            var furnitureNames = state.getFurnitureLayouts().stream()
                    .map(layout -> furnitureRepository.findById(layout.getFurnitureId())
                            .map(Furniture::getName)
                            .orElse("Unknown Furniture"))
                    .collect(Collectors.toList());

            return RoomPreviewDTO.builder()
                    .wallpaperName(wallpaperName)
                    .floorName(floorName)
                    .furnitureNames(furnitureNames)
                    .totalFurniture(furnitureNames.size())
                    .build();

        } catch (JsonProcessingException e) {
            log.error("방 미리보기 생성 실패", e);
            throw new RoomSerializationException();
        }
    }
}