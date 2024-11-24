package com.loveforest.loveforest.domain.room.service;

import com.loveforest.loveforest.domain.room.dto.*;
import com.loveforest.loveforest.domain.room.entity.Room;

import java.util.Optional;

/**
 * 모든 방 데코레이션 작업을 위한 통합 인터페이스
 * ISP를 고려하여 각 데코레이션 타입별 메서드 분리
 */
public interface RoomService {
    // 가구 관련 메서드
    RoomDecorationResponseDTO placeFurniture(Long coupleId, RoomDecorationRequestDTO request);
    RoomDecorationResponseDTO moveFurniture(Long furnitureLayoutId, RoomFurnitureUpdateRequestDTO request);
    RoomResponseDTO removeFurniture(Long furnitureLayoutId);

    // 벽지 관련 메서드
    RoomResponseDTO setWallpaper(Long coupleId, Long wallpaperId);
    RoomResponseDTO removeWallpaper(Long coupleId);

    // 바닥 관련 메서드
    RoomResponseDTO setFloor(Long coupleId, Long floorId);
    RoomResponseDTO removeFloor(Long coupleId);

    // 방 전체 상태 조회
    RoomResponseDTO getRoomStatus(Long coupleId);

    PublicRoomResponseDTO getPublicRoomInfo(Long coupleId, Long requesterId);

    Optional<Room> findById(Long roomId);
}
