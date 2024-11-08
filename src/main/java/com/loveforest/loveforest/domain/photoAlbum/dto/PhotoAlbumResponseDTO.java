package com.loveforest.loveforest.domain.photoAlbum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "사진첩 응답 DTO")
public class PhotoAlbumResponseDTO {

    @Schema(description = "사진 ID", example = "1")
    private Long id;

    @Schema(description = "이미지 URL", example = "https://s3.amazon.com/images/photo.jpg")
    private String imageUrl;

    @Schema(description = "3D 오브젝트 URL", example = "https://s3.amazon.com/objects/model.zip")
    private String objectUrl;

    @Schema(description = "X 좌표", example = "100.0")
    private Double positionX;

    @Schema(description = "Y 좌표", example = "200.0")
    private Double positionY;
}