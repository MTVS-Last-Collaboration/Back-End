package com.loveforest.loveforest.domain.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Schema(description = "방 꾸미기 응답 DTO")
@NoArgsConstructor
@AllArgsConstructor
public class RoomDecorationResponseDTO {
    @Schema(description = "가구 레이아웃 ID", example = "1")
    private Long layoutId;

    @Schema(description = "가구 ID", example = "1")
    private Long furnitureId;

    @Schema(description = "가구 이름", example = "클래식 소파")
    private String furnitureName;

    @Schema(description = "X 좌표", example = "100")
    private int positionX;

    @Schema(description = "Y 좌표", example = "200")
    private int positionY;

    @Schema(description = "회전 각도", example = "90")
    private int rotation;

    @Schema(description = "가구 너비", example = "2")
    private int width;

    @Schema(description = "가구 높이", example = "3")
    private int height;
}

