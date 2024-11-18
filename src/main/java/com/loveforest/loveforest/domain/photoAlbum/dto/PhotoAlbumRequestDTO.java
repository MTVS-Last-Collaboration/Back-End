package com.loveforest.loveforest.domain.photoAlbum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "사진첩 등록 요청 DTO")
public class PhotoAlbumRequestDTO {

    @Schema(description = "Base64로 인코딩된 이미지", required = true)
    @NotBlank(message = "이미지는 필수입니다.")
    private String base64Image;

    @Schema(description = "X 좌표", example = "100.0", required = true)
    @NotNull(message = "X 좌표는 필수입니다.")
    private Double positionX;

    @Schema(description = "Y 좌표", example = "200.0", required = true)
    @NotNull(message = "Y 좌표는 필수입니다.")
    private Double positionY;

    public PhotoAlbumRequestDTO(String base64Image, Double positionX, Double positionY) {
        this.base64Image = base64Image;
        this.positionX = positionX;
        this.positionY = positionY;
    }
}