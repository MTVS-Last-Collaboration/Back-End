package com.loveforest.loveforest.domain.photoAlbum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "사진첩 응답 DTO")
public class PhotoAlbumResponseDTO {
    @Schema(description = "사진 ID", example = "1")
    private Long id;

    @Schema(description = "제목", example = "우리의 첫 데이트")
    private String title;

    @Schema(description = "내용", example = "행복했던 그 날의 기록")
    private String content;

    @Schema(description = "사진 날짜", example = "2024-01-01T12:00:00")
    private LocalDate photoDate;

    @Schema(description = "이미지 URL")
    private String imageUrl;

    @Schema(description = "3D 오브젝트 URL")
    private String objectUrl;

    @Schema(description = "텍스처 이미지 URL")
    private String pngUrl;

    @Schema(description = "재질 파일 URL")
    private String materialUrl;

    @Schema(description = "X 좌표", example = "100.0")
    private Integer positionX;

    @Schema(description = "Y 좌표", example = "200.0")
    private Integer positionY;
}