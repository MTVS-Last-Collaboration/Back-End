package com.loveforest.loveforest.domain.room.dto;

import com.loveforest.loveforest.domain.room.enums.RoomStateSource;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CollectionRoomResponseDTO {
    private Long id;
    private RoomStateSource source;
    private LocalDateTime savedAt;
    private RoomPreviewDTO roomPreview;
}
