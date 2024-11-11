package com.loveforest.loveforest.domain.room.dto;

import lombok.Getter;

/**
 * 다른 커플의 방 조회 응답 DTO
 */
@Getter
public class PublicRoomApiResponseDTO extends ApiResponseDTO<PublicRoomResponseDTO> {
    public PublicRoomApiResponseDTO(PublicRoomResponseDTO data) {
        super("다른 커플의 방 조회가 완료되었습니다.", data);
    }
}