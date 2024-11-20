package com.loveforest.loveforest.domain.photoAlbum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Schema(description = "전시된 사진의 상세 정보 DTO")
@Getter
@Builder
public class PhotoDetailsDTO {
    @Schema(description = "사진 ID", example = "1")
    private Long photoId;

    @Schema(description = "사진 제목", example = "우리의 첫 데이트")
    private String title;

    @Schema(description = "원본 이미지 URL",
            example = "https://s3.amazonaws.com/bucket/original-image.jpg")
    private String imageUrl;

    @Schema(description = "사진 촬영 날짜", example = "2024-01-20")
    private LocalDate photoDate;

    @Schema(description = "사진 설명",
            example = "행복했던 그 날의 기록")
    private String description;
}