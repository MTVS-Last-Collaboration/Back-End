package com.loveforest.loveforest.domain.room.service;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.room.dto.RoomOperationResponseDTO;
import com.loveforest.loveforest.domain.room.dto.SharedRoomResponseDTO;
import com.loveforest.loveforest.domain.room.entity.Room;
import com.loveforest.loveforest.domain.room.exception.RoomNotFoundException;
import com.loveforest.loveforest.domain.room.repository.RoomRepository;
import com.loveforest.loveforest.domain.room.util.RoomPreviewMapper;
import com.loveforest.loveforest.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SharedRoomService {
    private final RoomRepository roomRepository;
    private final RoomPreviewMapper roomPreviewMapper;


    /**
     * 방 공유 설정
     */
    public RoomOperationResponseDTO setRoomSharing(Long coupleId, boolean isShared) {
        Room room = roomRepository.findByCoupleId(coupleId)
                .orElseThrow(RoomNotFoundException::new);

//        RoomCollection roomCollection = roomCollectionRepository.findByCoupleId(coupleId)
//                .orElseGet(() -> {
//                    Couple couple = coupleRepository.findById(coupleId)
//                            .orElseThrow(CoupleNotFoundException::new);
//                    return new RoomCollection(couple);
//                });
//        Long roomCollectionId = roomCollection.getId();
//        List<CollectionRoom> byCollectionCoupleId = collectionRoomRepository.findByCollectionCoupleId(roomCollectionId);



        room.updateSharing(isShared);
        roomRepository.save(room);

        log.info("방 공유 상태 변경 - 커플 ID: {}, 공유 상태: {}", coupleId, isShared);

        // 공유 상태가 true일 경우 thumbnailUrl 포함
        String thumbnailUrl = isShared ? room.getThumbnailUrl() : null;

        return RoomOperationResponseDTO.builder("방 공유 상태가 변경되었습니다.")
                .addData("isShared", isShared)
                .addData("thumbnailUrl", thumbnailUrl)
                .addData("updatedAt", LocalDateTime.now())
                .build();
    }

    /**
     * 공유된 방 목록 조회
     */
    public List<SharedRoomResponseDTO> getSharedRooms(Long coupleId) {
        return roomRepository.findBySharing_IsSharedTrue().stream()
                .filter(room -> !room.getCouple().getId().equals(coupleId)) // 자신의 방 제외
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    private SharedRoomResponseDTO convertToDTO(Room room) {
        return SharedRoomResponseDTO.builder()
                .roomId(room.getId())
                .coupleName(generateCoupleName(room.getCouple()))
                .roomPreview(roomPreviewMapper.createFromRoom(room))
                .sharedAt(room.getSharing().getLastModified())
                .thumbnailUrl(room.getThumbnailUrl())
                .build();
    }

    private String generateCoupleName(Couple couple) {
        List<User> users = couple.getUsers();
        if (users.size() >= 2) {
            return String.format("%s♥%s",
                    users.get(0).getNickname(),
                    users.get(1).getNickname());
        }
        return "커플";
    }

}