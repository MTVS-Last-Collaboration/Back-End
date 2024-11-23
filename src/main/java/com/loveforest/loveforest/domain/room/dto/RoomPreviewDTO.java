package com.loveforest.loveforest.domain.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "방 미리보기 정보")
public class RoomPreviewDTO {
    @Schema(description = "벽지 이름")
    private String wallpaperName;

    @Schema(description = "바닥 이름")
    private String floorName;

    @Schema(description = "가구 이름 목록")
    private List<String> furnitureNames;

    @Schema(description = "총 가구 수")
    private int totalFurniture;
}