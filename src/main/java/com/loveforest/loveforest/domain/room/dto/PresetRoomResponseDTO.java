package com.loveforest.loveforest.domain.room.dto;

import com.loveforest.loveforest.domain.room.entity.PresetRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "프리셋 룸 응답 DTO")
@Getter
@Builder
public class PresetRoomResponseDTO {
    @Schema(description = "프리셋 ID", example = "1")
    private Long presetId;

    @Schema(description = "프리셋 이름", example = "My First Room")
    private String name;

    @Schema(description = "벽지 정보")
    private WallpaperDTO wallpaper;

    @Schema(description = "바닥 정보")
    private FloorDTO floor;

    @Schema(description = "가구 배치 정보 목록")
    private List<PresetFurnitureLayoutDTO> furnitureLayouts;

    public static PresetRoomResponseDTO from(PresetRoom preset) {
        return PresetRoomResponseDTO.builder()
                .presetId(preset.getId())
                .name(preset.getName())
                .wallpaper(WallpaperDTO.from(preset.getWallpaper()))
                .floor(FloorDTO.from(preset.getFloor()))
                .furnitureLayouts(preset.getFurnitureLayouts().stream()
                        .map(PresetFurnitureLayoutDTO::from)
                        .collect(Collectors.toList()))
                .build();
    }
}