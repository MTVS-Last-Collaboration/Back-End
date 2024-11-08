package com.loveforest.loveforest.domain.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Schema(description = "공개된 방 정보 응답 DTO")
@Getter
@AllArgsConstructor
public class PublicRoomResponseDTO {
    @Schema(description = "방 ID", example = "1")
    private Long roomId;

    @Schema(description = "커플 ID", example = "1")
    private Long coupleId;

    @Schema(description = "방에 배치된 가구 목록")
    private List<PublicFurnitureDTO> furnitureLayouts;

    @Getter
    @AllArgsConstructor
    public static class PublicFurnitureDTO {
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
    }
}