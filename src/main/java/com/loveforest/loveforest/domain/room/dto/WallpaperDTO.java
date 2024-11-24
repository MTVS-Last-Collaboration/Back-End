package com.loveforest.loveforest.domain.room.dto;

import com.loveforest.loveforest.domain.room.entity.Wallpaper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "벽지 정보 DTO")
@Getter
@Builder
public class WallpaperDTO {
    @Schema(description = "벽지 ID", example = "1")
    private Long id;

    @Schema(description = "벽지 이름", example = "클래식 벽지")
    private String name;

    @Schema(description = "벽지 번호", example = "1")
    private int wallpaperNumber;

    public static WallpaperDTO from(Wallpaper wallpaper) {
        if (wallpaper == null) {
            return null;
        }

        return WallpaperDTO.builder()
                .id(wallpaper.getId())
                .name(wallpaper.getName())
                .wallpaperNumber(wallpaper.getWallpaperNumber())
                .build();
    }
}

