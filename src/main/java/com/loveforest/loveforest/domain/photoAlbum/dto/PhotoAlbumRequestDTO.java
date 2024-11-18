package com.loveforest.loveforest.domain.photoAlbum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@Schema(description = "사진첩 등록 요청 DTO")
public class PhotoAlbumRequestDTO {
    @Schema(description = "제목", example = "우리의 첫 데이트")
    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @Schema(description = "내용", example = "행복했던 그 날의 기록")
    @NotBlank(message = "내용은 필수입니다")
    private String content;

    @Schema(description = "사진 날짜", example = "2024-01-01T12:00:00")
    @NotNull(message = "날짜는 필수입니다")
    private LocalDateTime photoDate;

    @Schema(description = "이미지 파일", type = "string", format = "binary")
    @NotNull(message = "사진은 필수입니다")
    private MultipartFile photo;
}