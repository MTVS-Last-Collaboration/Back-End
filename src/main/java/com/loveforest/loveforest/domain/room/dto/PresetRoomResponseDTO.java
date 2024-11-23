package com.loveforest.loveforest.domain.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "프리셋 방 정보")
@Getter
@Builder
public class PresetRoomResponseDTO {
    @Schema(description = "프리셋 ID")
    private Long id;

    @Schema(description = "프리셋 이름")
    private String name;

    @Schema(description = "방 미리보기 정보")
    private RoomPreviewDTO preview;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;
}