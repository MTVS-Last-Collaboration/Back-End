package com.loveforest.loveforest.domain.room.dto;

import lombok.Getter;

/**
 * 가구 배치 응답 DTO
 */
@Getter
public class RoomDecorationApiResponseDTO extends ApiResponseDTO<RoomDecorationResponseDTO> {
    public RoomDecorationApiResponseDTO(RoomDecorationResponseDTO data) {
        super("가구 배치가 완료되었습니다.", data);
    }

    public RoomDecorationApiResponseDTO(String message, RoomDecorationResponseDTO data) {
        super(message, data);
    }
}

