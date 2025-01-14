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

    @Schema(description = "썸네일 URL", example = "https://example.com/thumbnail.jpg")
    private String thumbnailUrl;

    public static PresetRoomResponseDTO from(PresetRoom preset) {
        return PresetRoomResponseDTO.builder()
                .presetId(preset.getId())
                .name(preset.getName())
                .wallpaper(preset.getWallpaper() != null ? WallpaperDTO.from(preset.getWallpaper()) : null)
                .floor(preset.getFloor() != null ? FloorDTO.from(preset.getFloor()) : null)
                .furnitureLayouts(preset.getFurnitureLayouts().stream()
                        .map(PresetFurnitureLayoutDTO::from)
                        .collect(Collectors.toList()))
                .thumbnailUrl(preset.getThumbnailUrl())
                .build();
    }
}