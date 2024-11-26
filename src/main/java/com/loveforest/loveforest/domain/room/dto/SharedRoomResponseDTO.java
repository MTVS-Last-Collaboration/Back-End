package com.loveforest.loveforest.domain.room.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "공유된 방 응답 DTO")
public class SharedRoomResponseDTO {

    @Schema(description = "방 ID", example = "1")
    private Long roomId;

    @Schema(description = "커플 이름", example = "철수♥영희")
    private String coupleName;

    @Schema(description = "방 미리보기 정보")
    private RoomPreviewDTO roomPreview;

    @Schema(description = "방 공유 시각", example = "2024-03-19T15:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sharedAt;

    @Schema(description = "방 썸네일 URL", example = "https://example.com/thumbnails/shared_room1.jpg")
    private String thumbnailUrl;
}
