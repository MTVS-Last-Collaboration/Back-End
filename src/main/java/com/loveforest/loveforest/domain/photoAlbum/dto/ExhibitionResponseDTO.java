package com.loveforest.loveforest.domain.photoAlbum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "전시물 응답 DTO")
public class ExhibitionResponseDTO {
    @Schema(description = "전시 ID", example = "1")
    private Long exhibitionId;

    @Schema(description = "3D 오브젝트 파일 URL", example = "https://s3.amazonaws.com/bucket/object.obj")
    private String objectUrl;

    @Schema(description = "텍스처 이미지 URL", example = "https://s3.amazonaws.com/bucket/texture.png")
    private String textureUrl;

    @Schema(description = "재질 파일 URL", example = "https://s3.amazonaws.com/bucket/material.mtl")
    private String materialUrl;

    @Schema(description = "X축 위치", example = "10.5")
    private Integer positionX;

    @Schema(description = "Y축 위치", example = "20.3")
    private Integer positionY;

    @Schema(description = "전시 시작 시간", example = "2024-01-20T14:30:00")
    private LocalDateTime exhibitedAt;

    @Schema(description = "전시된 사진 정보")
    private PhotoDetailsDTO photo;
}