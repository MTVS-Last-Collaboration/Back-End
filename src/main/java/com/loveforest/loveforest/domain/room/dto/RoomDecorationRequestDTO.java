package com.loveforest.loveforest.domain.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "가구 배치 요청 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
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

    @Schema(description = "가구의 너비", example = "50", required = true)
    private int width;

    @Schema(description = "가구의 높이", example = "30", required = true)
    private int height;
}
