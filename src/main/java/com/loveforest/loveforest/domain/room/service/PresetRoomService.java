package com.loveforest.loveforest.domain.room.service;

import com.loveforest.loveforest.domain.room.entity.FurnitureLayout;
import com.loveforest.loveforest.domain.room.entity.PresetFurnitureLayout;
import com.loveforest.loveforest.domain.room.entity.PresetRoom;
import com.loveforest.loveforest.domain.room.entity.Room;
import com.loveforest.loveforest.domain.room.exception.RoomNotFoundException;
import com.loveforest.loveforest.domain.room.repository.PresetFurnitureLayoutRepository;
import com.loveforest.loveforest.domain.room.repository.PresetRoomRepository;
import com.loveforest.loveforest.domain.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PresetRoomService {
    private final RoomRepository roomRepository;
    private final PresetRoomRepository presetRoomRepository;
    private final PresetFurnitureLayoutRepository presetFurnitureLayoutRepository;

    public PresetRoom saveRoomAsPreset(Long roomId, String presetName) {
        // 현재 방 상태 조회
        Room currentRoom = roomRepository.findById(roomId)
                .orElseThrow(RoomNotFoundException::new);

        // 프리셋 룸 생성
        PresetRoom presetRoom = PresetRoom.builder()
                .name(presetName)
                .wallpaper(currentRoom.getWallpaper())
                .floor(currentRoom.getFloor())
                .build();

        // 가구 배치 정보 복사
        for (FurnitureLayout layout : currentRoom.getFurnitureLayouts()) {
            PresetFurnitureLayout presetLayout = PresetFurnitureLayout.builder()
                    .furniture(layout.getFurniture())
                    .positionX(layout.getPositionX())
                    .positionY(layout.getPositionY())
                    .rotation(layout.getRotation())
                    .build();
            presetRoom.addFurnitureLayout(presetLayout);
        }

        return presetRoomRepository.save(presetRoom);
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
}
