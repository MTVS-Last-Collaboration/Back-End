package com.loveforest.loveforest.domain.room.service;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.room.dto.RoomPreviewDTO;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SharedRoomService {
    private final RoomRepository roomRepository;
    private final RoomCollectionService roomCollectionService;
    private final RoomPreviewMapper roomPreviewMapper;

    /**
     * 방 공유 설정
     */
    public void setRoomSharing(Long coupleId, boolean isShared) {
        Room room = roomRepository.findByCoupleId(coupleId)
                .orElseThrow(RoomNotFoundException::new);

        room.updateSharing(isShared);
        roomRepository.save(room);

        log.info("방 공유 상태 변경 - 커플 ID: {}, 공유 상태: {}", coupleId, isShared);
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