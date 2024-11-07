package com.loveforest.loveforest.domain.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "가구 위치 이동 요청 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomFurnitureUpdateRequestDTO {

    @Schema(description = "새로운 X 좌표", example = "150", required = true)
    private int positionX;

    @Schema(description = "새로운 Y 좌표", example = "250", required = true)
    private int positionY;

    @Schema(description = "새로운 회전 각도", example = "180", required = true)
    private int rotation;
}
