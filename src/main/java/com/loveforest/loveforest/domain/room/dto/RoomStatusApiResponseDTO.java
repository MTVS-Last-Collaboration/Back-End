package com.loveforest.loveforest.domain.room.dto;

import lombok.Getter;

/**
 * 방 상태 조회 응답 DTO
 */
@Getter
public class RoomStatusApiResponseDTO extends ApiResponseDTO<RoomResponseDTO> {
    public RoomStatusApiResponseDTO(RoomResponseDTO data) {
        super("방 상태 조회가 완료되었습니다.", data);
    }

    // 커스텀 메시지를 받는 생성자 추가
    public RoomStatusApiResponseDTO(String message, RoomResponseDTO data) {
        super(message, data);
    }
}