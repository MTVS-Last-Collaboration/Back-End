package com.loveforest.loveforest.domain.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "가구 배치 요청 정보")
@Getter
public class RoomDecorationRequestDTO {

    @Schema(description = "커플 ID", example = "1", required = true)
    private Long coupleId;

    @Schema(description = "가구 ID", example = "1", required = true)
    private Long furnitureId;

    @Schema(description = "X 좌표", example = "100", required = true)
    private int positionX;

    @Schema(description = "Y 좌표", example = "200", required = true)
    private int positionY;

    @Schema(description = "회전 각도", example = "90", required = true)
    private int rotation;
}
