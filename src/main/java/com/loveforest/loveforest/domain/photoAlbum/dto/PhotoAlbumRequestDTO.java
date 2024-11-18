package com.loveforest.loveforest.domain.photoAlbum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Schema(description = "사진첩 등록 요청 DTO")
public class PhotoAlbumRequestDTO {
    @Schema(
            description = "이미지 파일",
            required = true,
            type = "string",
            format = "binary"
    )
    private MultipartFile photo;

    public PhotoAlbumRequestDTO(MultipartFile photo) {
        this.photo = photo;
    }
}