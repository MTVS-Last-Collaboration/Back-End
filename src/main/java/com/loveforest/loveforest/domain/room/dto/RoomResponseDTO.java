package com.loveforest.loveforest.domain.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Schema(description = "방 상태 응답 정보")
@Getter
@AllArgsConstructor
public class RoomResponseDTO {

    @Schema(description = "방 ID", example = "1")
    private Long roomId;

    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;

    @Schema(description = "방에 배치된 가구 목록")
    private List<FurnitureLayoutDTO> furnitureLayouts;

    @Schema(description = "방의 바닥 정보")
    private FloorDTO floor;

    @Schema(description = "방의 벽지 정보")
    private WallpaperDTO wallpaper;

    @Getter
    @AllArgsConstructor
    public static class FurnitureLayoutDTO {

        @Schema(description = "가구 배치 정보 ID", example = "1")
        private Long furnitureLayoutId;


        @Schema(description = "가구 ID", example = "1")
        private Long furnitureId;

        @Schema(description = "가구 이름", example = "의자")
        private String furnitureName;

        @Schema(description = "X 좌표", example = "100")
        private int positionX;

        @Schema(description = "Y 좌표", example = "200")
        private int positionY;

        @Schema(description = "회전 각도", example = "90")
        private int rotation;

        @Schema(description = "가구의 너비", example = "50")
        private int width;

        @Schema(description = "가구의 높이", example = "30")
        private int height;
    }

    @Getter
    @AllArgsConstructor
    public static class FloorDTO {
        @Schema(description = "바닥 ID", example = "1")
        private Long id;

        @Schema(description = "바닥 이름", example = "고급 나무 바닥")
        private String name;

        @Schema(description = "바닥 번호", example = "3")
        private int floorNumber;
    }

    @Getter
    @AllArgsConstructor
    public static class WallpaperDTO {
        @Schema(description = "벽지 ID", example = "1")
        private Long id;

        @Schema(description = "벽지 이름", example = "모던 벽지")
        private String name;

        @Schema(description = "벽지 번호", example = "5")
        private int wallpaperNumber;
    }
}