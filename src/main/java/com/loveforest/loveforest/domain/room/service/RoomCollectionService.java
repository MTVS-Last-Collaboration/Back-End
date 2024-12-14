package com.loveforest.loveforest.domain.room.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.exception.CoupleNotFoundException;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.room.dto.CollectionRoomResponseDTO;
import com.loveforest.loveforest.domain.room.dto.RoomOperationResponseDTO;
import com.loveforest.loveforest.domain.room.entity.CollectionRoom;
import com.loveforest.loveforest.domain.room.entity.PresetRoom;
import com.loveforest.loveforest.domain.room.entity.Room;
import com.loveforest.loveforest.domain.room.entity.RoomCollection;
import com.loveforest.loveforest.domain.room.enums.RoomStateSource;
import com.loveforest.loveforest.domain.room.exception.*;
import com.loveforest.loveforest.domain.room.repository.*;
import com.loveforest.loveforest.domain.room.util.RoomPreviewMapper;
import com.loveforest.loveforest.exception.CustomException;
import com.loveforest.loveforest.exception.ErrorCode;
import com.loveforest.loveforest.exception.common.UnauthorizedException;
import com.loveforest.loveforest.s3.service.LocalStorageService;
import com.loveforest.loveforest.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;
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
    private final WallpaperRepository wallpaperRepository;
    private final FloorRepository floorRepository;
//    private final S3Service s3Service;
    private final LocalStorageService storageService;

    @Value("${server.url}")
    private String serverUrl;

    @Value("${file.storage.path}")
    private String storagePath;

    /**
     * 컬렉션에 현재 방 상태 저장
     */
    public RoomOperationResponseDTO saveCurrentRoom(Long coupleId, MultipartFile thumbnail) {
        Room currentRoom = roomRepository.findByCoupleId(coupleId)
                .orElseThrow(RoomNotFoundException::new);

        RoomCollection collection = getOrCreateCollection(coupleId);

//        String imageUrl = null;
        String fileUrl = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                validateImage(thumbnail);
//                imageUrl = uploadImage(thumbnail);
                fileUrl = uploadImage(thumbnail);
            } catch (Exception e) {
                log.error("이미지 업로드 실패: {}", e.getMessage());
                throw new CustomException(ErrorCode.ROOM_IMAGE_UPLOAD_FAILED);
            }
        }

//        currentRoom.updateThumbnail(imageUrl);
        currentRoom.updateThumbnail(fileUrl);
        roomRepository.save(currentRoom);

        CollectionRoom collectionRoom = CollectionRoom.builder()
                .collection(collection)
                .roomData(currentRoom.serializeState())
                .source(RoomStateSource.CURRENT)
//                .thumbnailUrl(imageUrl)
                .thumbnailUrl(fileUrl)
                .build();

        collection.getSavedRooms().add(collectionRoom);
        collectionRepository.save(collection);

        log.info("현재 방 상태 저장 완료 - 커플 ID: {}, 이미지: {}",
                coupleId, /*imageUrl*/fileUrl != null ? fileUrl : "없음");

        return RoomOperationResponseDTO.forSaveState(/*imageUrl*/fileUrl);
    }

    /**
     * 프리셋 방 상태 저장
     */
    @Transactional
    public RoomOperationResponseDTO savePresetRoom(Long coupleId, Long presetId) {
        PresetRoom preset = presetRoomRepository.findById(presetId)
                .orElseThrow(PresetNotFoundException::new);
        String imageUrl = preset.getThumbnailUrl();

        RoomCollection collection = getOrCreateCollection(coupleId);

        CollectionRoom collectionRoom = CollectionRoom.builder()
                .collection(collection)
                .roomData(createRoomStateJson(preset))
                .source(RoomStateSource.PRESET)
                .thumbnailUrl(imageUrl)
                .build();

        collection.getSavedRooms().add(collectionRoom);
        collectionRepository.save(collection);

        log.info("프리셋 방 상태 저장 완료 - 커플 ID: {}, 프리셋 ID: {}, 이미지: {}",
                coupleId, presetId, imageUrl != null ? "저장됨" : "없음");

        return RoomOperationResponseDTO.forPresetApply(collectionRoom.getId(), presetId);
    }

    /**
     * 공유된 방 상태 저장
     */
    public RoomOperationResponseDTO saveSharedRoom(Long coupleId, Long sharedRoomId/*, MultipartFile thumbnail*/) {
        Room sharedRoom = roomRepository.findById(sharedRoomId)
                .orElseThrow(RoomNotFoundException::new);

        if (!sharedRoom.isShared()) {
            throw new RoomNotSharedException();
        }

        if (sharedRoom.getCouple().getId().equals(coupleId)) {
            throw new InvalidOperationException();
        }
        String imageUrl = sharedRoom.getThumbnailUrl();

        RoomCollection collection = getOrCreateCollection(coupleId);

        CollectionRoom collectionRoom = CollectionRoom.builder()
                .collection(collection)
                .roomData(sharedRoom.serializeState())
                .source(RoomStateSource.SHARED)
                .thumbnailUrl(imageUrl)
                .build();

        collection.getSavedRooms().add(collectionRoom);
        collectionRepository.save(collection);

        return RoomOperationResponseDTO.builder("공유된 방이 성공적으로 저장되었습니다.")
                .addData("sharedRoomId", sharedRoomId)
                .addData("thumbnailUrl", imageUrl)
                .build();
    }

    /**
     * PresetRoom의 상태를 JSON으로 변환하는 헬퍼 메서드
     */
    private String createRoomStateJson(PresetRoom preset) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> roomState = new HashMap<>();

            // 벽지 정보
            if (preset.getWallpaper() != null) {
                roomState.put("wallpaperId", preset.getWallpaper().getId());
            }

            // 바닥 정보
            if (preset.getFloor() != null) {
                roomState.put("floorId", preset.getFloor().getId());
            }

            // 가구 배치 정보
            List<Map<String, Object>> furnitureLayouts = preset.getFurnitureLayouts().stream()
                    .map(layout -> {
                        Map<String, Object> layoutInfo = new HashMap<>();
                        layoutInfo.put("furnitureId", layout.getFurniture().getId());
                        layoutInfo.put("positionX", layout.getPositionX());
                        layoutInfo.put("positionY", layout.getPositionY());
                        layoutInfo.put("rotation", layout.getRotation());
                        return layoutInfo;
                    })
                    .collect(Collectors.toList());

            roomState.put("furnitureLayouts", furnitureLayouts);

            return objectMapper.writeValueAsString(roomState);
        } catch (JsonProcessingException e) {
            log.error("방 상태 JSON 변환 실패", e);
            throw new InvalidOperationException();
        }
    }


    /**
     * 저장된 방 상태를 현재 방에 적용
     */
    public void applyRoomState(Long coupleId, Long collectionRoomId) {
        CollectionRoom savedRoom = collectionRoomRepository.findById(collectionRoomId)
                .orElseThrow(CollectionRoomNotFoundException::new);

        // 커플 접근 권한 검증
        validateCoupleAccess(coupleId, savedRoom);

        // 현재 커플의 방 가져오기
        Room currentRoom = roomRepository.findByCoupleId(coupleId)
                .orElseThrow(RoomNotFoundException::new);

        // 저장된 방 상태를 현재 방으로 복원
        currentRoom.restoreState(
                savedRoom.getRoomData(),
                furnitureRepository,
                wallpaperRepository, // 추가된 wallpaperRepository 전달
                floorRepository      // 추가된 floorRepository 전달
        );

        // 복원된 방 상태 저장
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
                .thumbnailUrl(room.getThumbnailUrl())
                .build();
    }

    // 이미지 처리를 위한 유틸리티 메서드들
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
        }

        if (file.getSize() > 5_000_000) { // 5MB
            throw new CustomException(ErrorCode.IMAGE_SIZE_EXCEEDED);
        }
    }

    private String processAndUploadImage(MultipartFile thumbnail) {
        if (thumbnail == null || thumbnail.isEmpty()) {
            return null;
        }

        try {
            validateImage(thumbnail);
            return uploadImage(thumbnail);
        } catch (IOException e) {
            log.error("이미지 업로드 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.ROOM_IMAGE_UPLOAD_FAILED);
        }
    }

    private String uploadImage(MultipartFile file) throws IOException {
//        return s3Service.uploadFile(
//                file.getBytes(),
//                getFileExtension(file.getOriginalFilename()),
//                file.getContentType(),
//                file.getSize()
//        );
        // UUID를 사용하여 고유한 파일명 생성
        String uniqueFileName = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());

        // 파일 저장
        String savedFileName = storageService.uploadFile(
                file.getBytes(),
                getFileExtension(file.getOriginalFilename()),
                file.getContentType(),
                file.getSize()
        );

        // 전체 URL 생성 및 반환
        return UriComponentsBuilder.fromUriString(serverUrl)
                .pathSegment(storagePath)
                .pathSegment(savedFileName)
                .build()
                .toUriString();
    }

    private String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".")))
                .orElse(".jpg");
    }

}