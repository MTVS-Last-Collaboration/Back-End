package com.loveforest.loveforest.domain.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 다른 커플의 방 정보를 위한 DTO
 * 필요한 정보만 선택적으로 노출
 */
@Getter
@Builder
@Schema(description = "공개된 방 정보 응답 DTO")
public class PublicRoomResponseDTO {
    @Schema(description = "방 ID", example = "1")
    private final Long roomId;

    @Schema(description = "커플 ID", example = "1")
    private final Long coupleId;

    @Schema(description = "커플 닉네임", example = "행복한 커플")
    private final String coupleName;

    @Schema(description = "연애 시작일", example = "2024-01-01")
    private final LocalDate anniversaryDate;

    @Schema(description = "방 스타일 정보")
    private final RoomStyleDTO style;

    @Schema(description = "방에 배치된 가구 목록")
    private final List<PublicFurnitureDTO> furnitureLayouts;

    @Schema(description = "방 썸네일 사진")
    private final String thumbnailUrl;

    @Getter
    @Builder
    public static class RoomStyleDTO {
        @Schema(description = "벽지 이름", example = "클래식 벽지")
        private final String wallpaperName;

        @Schema(description = "바닥 이름", example = "원목 바닥")
        private final String floorName;
    }

    @Getter
    @Builder
    public static class PublicFurnitureDTO {
        @Schema(description = "가구 ID", example = "1")
        private final Long furnitureId;

        @Schema(description = "가구 이름", example = "클래식 소파")
        private final String furnitureName;

        @Schema(description = "가구 타입", example = "SOFA")
        private final String furnitureType;

        @Schema(description = "X 좌표", example = "100")
        private final int positionX;

        @Schema(description = "Y 좌표", example = "200")
        private final int positionY;

        @Schema(description = "회전 각도", example = "90")
        private final int rotation;
    }
}