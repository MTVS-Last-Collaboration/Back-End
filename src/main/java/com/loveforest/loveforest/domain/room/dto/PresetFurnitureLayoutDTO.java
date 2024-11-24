package com.loveforest.loveforest.domain.room.dto;

import com.loveforest.loveforest.domain.room.entity.PresetFurnitureLayout;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "프리셋 가구 배치 정보 DTO")
@Getter
@Builder
public class PresetFurnitureLayoutDTO {
    @Schema(description = "가구 ID", example = "1")
    private Long furnitureId;

    @Schema(description = "가구 이름", example = "클래식 소파")
    private String name;

    @Schema(description = "X 좌표", example = "100")
    private int positionX;

    @Schema(description = "Y 좌표", example = "200")
    private int positionY;

    @Schema(description = "회전 각도", example = "90")
    private int rotation;

    public static PresetFurnitureLayoutDTO from(PresetFurnitureLayout layout) {
        return PresetFurnitureLayoutDTO.builder()
                .furnitureId(layout.getFurniture().getId())
                .name(layout.getFurniture().getName())
                .positionX(layout.getPositionX())
                .positionY(layout.getPositionY())
                .rotation(layout.getRotation())
                .build();
    }
}