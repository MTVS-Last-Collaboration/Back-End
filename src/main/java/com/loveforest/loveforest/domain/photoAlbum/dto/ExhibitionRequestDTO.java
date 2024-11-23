package com.loveforest.loveforest.domain.photoAlbum.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // Jackson 역직렬화를 위해 기본 생성자 추가
@Schema(description = "전시물 등록 요청 DTO")
public class ExhibitionRequestDTO {
    @Schema(description = "전시할 사진 ID", example = "1", required = true)
    @NotNull(message = "사진 ID는 필수입니다")
    private Long photoId;

    @Schema(description = "X축 위치 값", example = "10.5", required = true)
    @NotNull(message = "X축 위치는 필수입니다")
    private Double positionX; // Integer -> Double로 변경

    @Schema(description = "Y축 위치 값", example = "20.3", required = true)
    @NotNull(message = "Y축 위치는 필수입니다")
    private Double positionY; // Integer -> Double로 변경

    @Builder
    public ExhibitionRequestDTO(Long photoId, Double positionX, Double positionY) {
        this.photoId = photoId;
        this.positionX = positionX;
        this.positionY = positionY;
    }
}


