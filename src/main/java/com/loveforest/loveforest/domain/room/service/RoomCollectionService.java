package com.loveforest.loveforest.domain.room.service;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.exception.CoupleNotFoundException;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.room.dto.CollectionRoomResponseDTO;
import com.loveforest.loveforest.domain.room.entity.CollectionRoom;
import com.loveforest.loveforest.domain.room.entity.PresetRoom;
import com.loveforest.loveforest.domain.room.entity.Room;
import com.loveforest.loveforest.domain.room.entity.RoomCollection;
import com.loveforest.loveforest.domain.room.enums.RoomStateSource;
import com.loveforest.loveforest.domain.room.exception.*;
import com.loveforest.loveforest.domain.room.repository.*;
import com.loveforest.loveforest.domain.room.util.RoomPreviewMapper;
import com.loveforest.loveforest.exception.common.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RoomCollectionService {
    private final RoomCollectionRepository collectionRepository;
    private final CollectionRoomRepository collectionRoomRepository;
    private final RoomRepository roomRepository;
    private final PresetRoomRepository presetRoomRepository;
    private final CoupleRepository coupleRepository;
    private final RoomPreviewMapper roomPreviewMapper;
    private final FurnitureRepository furnitureRepository;

    /**
     * 컬렉션에 현재 방 상태 저장
     */
    public void saveCurrentRoom(Long coupleId) {
        Room currentRoom = roomRepository.findByCoupleId(coupleId)
                .orElseThrow(RoomNotFoundException::new);

        RoomCollection collection = getOrCreateCollection(coupleId);
        collection.addRoom(currentRoom, RoomStateSource.CURRENT);

        collectionRepository.save(collection);
        log.info("현재 방 상태 저장 완료 - 커플 ID: {}", coupleId);
    }

    /**
     * 프리셋 방 상태 저장
     */
    public void savePresetRoom(Long coupleId, Long presetId) {
        PresetRoom preset = presetRoomRepository.findById(presetId)
                .orElseThrow(PresetNotFoundException::new);

        RoomCollection collection = getOrCreateCollection(coupleId);

        // 프리셋의 roomData를 직접 사용
        CollectionRoom collectionRoom = new CollectionRoom(
                collection,
                preset.getRoomData(),
                RoomStateSource.PRESET
        );

        collection.getSavedRooms().add(collectionRoom);
        collectionRepository.save(collection);

        log.info("프리셋 방 상태 저장 완료 - 커플 ID: {}, 프리셋 ID: {}", coupleId, presetId);
    }

    /**
     * 공유된 방 상태 저장
     */
    public void saveSharedRoom(Long coupleId, Long sharedRoomId) {
        Room sharedRoom = roomRepository.findById(sharedRoomId)
                .orElseThrow(RoomNotFoundException::new);

        // 공유 상태 확인
        if (!sharedRoom.isShared()) {
            throw new RoomNotSharedException();
        }

        // 자신의 방은 저장 불가
        if (sharedRoom.getCouple().getId().equals(coupleId)) {
            throw new InvalidOperationException();
        }

        RoomCollection collection = getOrCreateCollection(coupleId);
        collection.addRoom(sharedRoom, RoomStateSource.SHARED);

        collectionRepository.save(collection);
        log.info("공유 방 상태 저장 완료 - 커플 ID: {}, 공유방 ID: {}", coupleId, sharedRoomId);
    }

    /**
     * 저장된 방 상태를 현재 방에 적용
     */
    public void applyRoomState(Long coupleId, Long collectionRoomId) {
        CollectionRoom savedRoom = collectionRoomRepository.findById(collectionRoomId)
                .orElseThrow(CollectionRoomNotFoundException::new);

        validateCoupleAccess(coupleId, savedRoom);

        Room currentRoom = roomRepository.findByCoupleId(coupleId)
                .orElseThrow(RoomNotFoundException::new);

        currentRoom.restoreState(savedRoom.getRoomData(), furnitureRepository);
        roomRepository.save(currentRoom);
    }

    /**
     * 저장된 방 상태 조회
     */
    public List<CollectionRoomResponseDTO> getSavedRooms(Long coupleId) {
        List<CollectionRoom> savedRooms = collectionRoomRepository
                .findByCollectionCoupleId(coupleId);

        return savedRooms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private RoomCollection getOrCreateCollection(Long coupleId) {
        return collectionRepository.findByCoupleId(coupleId)
                .orElseGet(() -> {
                    Couple couple = coupleRepository.findById(coupleId)
                            .orElseThrow(CoupleNotFoundException::new);
                    return new RoomCollection(couple);
                });
    }

    private void validateCoupleAccess(Long coupleId, CollectionRoom room) {
        if (!room.getCollection().getCouple().getId().equals(coupleId)) {
            throw new UnauthorizedException();
        }
    }

    private CollectionRoomResponseDTO convertToDTO(CollectionRoom room) {
        return CollectionRoomResponseDTO.builder()
                .id(room.getId())
                .source(room.getSource())
                .savedAt(room.getSavedAt())
                .roomPreview(roomPreviewMapper.createFromJson(room.getRoomData()))
                .build();
    }

}