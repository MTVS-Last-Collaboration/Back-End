package com.loveforest.loveforest.domain.room.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SharedRoomResponseDTO {
    private Long roomId;
    private String coupleName;
    private RoomPreviewDTO roomPreview;
    private LocalDateTime sharedAt;
}
