package com.loveforest.loveforest.domain.photoAlbum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "3D 변환 요청 DTO")
public class Photo3DConvertRequestDTO {
    @Schema(description = "X 좌표", example = "100.0", required = true)
    @NotNull(message = "X 좌표는 필수입니다.")
    private Double positionX;

    @Schema(description = "Y 좌표", example = "200.0", required = true)
    @NotNull(message = "Y 좌표는 필수입니다.")
    private Double positionY;
}
