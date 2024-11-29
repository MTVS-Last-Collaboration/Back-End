package com.loveforest.loveforest.domain.room.service;

import com.loveforest.loveforest.domain.room.dto.PresetRoomResponseDTO;
import com.loveforest.loveforest.domain.room.entity.FurnitureLayout;
import com.loveforest.loveforest.domain.room.entity.PresetFurnitureLayout;
import com.loveforest.loveforest.domain.room.entity.PresetRoom;
import com.loveforest.loveforest.domain.room.entity.Room;
import com.loveforest.loveforest.domain.room.exception.PresetSaveFailedException;
import com.loveforest.loveforest.domain.room.exception.RoomNotFoundException;
import com.loveforest.loveforest.domain.room.repository.PresetRoomRepository;
import com.loveforest.loveforest.domain.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PresetRoomService {

    private final RoomRepository roomRepository;
    private final PresetRoomRepository presetRoomRepository;
    private final RoomService roomService;

    @Transactional
    public PresetRoomResponseDTO saveRoomAsPreset(Long roomId, String presetName) {
        try {
            // 현재 방 상태 조회
            Room currentRoom = roomService.findById(roomId)
                    .orElseThrow(RoomNotFoundException::new);

            // PresetRoom 생성
            PresetRoom presetRoom = PresetRoom.builder()
                    .name(presetName)
                    .wallpaper(currentRoom.getWallpaper())
                    .floor(currentRoom.getFloor())
                    .build();

            // 가구 배치 정보 복사
            currentRoom.getFurnitureLayouts().forEach(layout -> {
                PresetFurnitureLayout presetLayout = PresetFurnitureLayout.builder()
                        .furniture(layout.getFurniture())
                        .positionX(layout.getPositionX())
                        .positionY(layout.getPositionY())
                        .rotation(layout.getRotation())
                        .build();
                presetRoom.addFurnitureLayout(presetLayout);
            });

            // 저장
            PresetRoom savedPreset = presetRoomRepository.save(presetRoom);

            // DTO 변환 및 반환
            return PresetRoomResponseDTO.from(savedPreset);

        } catch (Exception e) {
            log.error("프리셋 저장 중 오류 발생", e);
            throw new PresetSaveFailedException();
        }
    }

    // 프리셋을 실제 방에 적용하는 메서드
    public void applyPresetToRoom(Long roomId, Long presetId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(RoomNotFoundException::new);

        PresetRoom preset = presetRoomRepository.findById(presetId)
                .orElseThrow(() -> new IllegalArgumentException("Preset not found"));

        // 방 상태 업데이트
        room.setWallpaper(preset.getWallpaper());
        room.setFloor(preset.getFloor());

        // 기존 가구 배치 제거
        room.getFurnitureLayouts().clear();

        // 프리셋의 가구 배치 적용
        for (PresetFurnitureLayout presetLayout : preset.getFurnitureLayouts()) {
            FurnitureLayout newLayout = new FurnitureLayout(
                    presetLayout.getFurniture(),
                    presetLayout.getPositionX(),
                    presetLayout.getPositionY(),
                    presetLayout.getRotation()
            );
            room.addFurnitureLayout(newLayout);
        }

        roomRepository.save(room);
    }

    /**
     * 모든 프리셋 방 목록 조회
     */
    public List<PresetRoomResponseDTO> getAllPresetRooms() {
        List<PresetRoom> presetRooms = presetRoomRepository.findAll();

        // 엔티티를 DTO로 변환
        List<PresetRoomResponseDTO> responseDTOs = presetRooms.stream()
                .map(PresetRoomResponseDTO::from)
                .toList();

        log.info("총 {}개의 프리셋 방을 조회했습니다.", responseDTOs.size());
        return responseDTOs;
    }
}
