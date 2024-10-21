package com.loveforest.loveforest.domain.room.service;


import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.room.dto.RoomDecorationRequestDTO;
import com.loveforest.loveforest.domain.room.dto.RoomResponseDTO;
import com.loveforest.loveforest.domain.room.entity.Furniture;
import com.loveforest.loveforest.domain.room.entity.FurnitureLayout;
import com.loveforest.loveforest.domain.room.entity.Room;
import com.loveforest.loveforest.domain.room.repository.FurnitureRepository;
import com.loveforest.loveforest.domain.room.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final CoupleRepository coupleRepository;
    private final FurnitureRepository furnitureRepository;

    public RoomService(RoomRepository roomRepository, CoupleRepository coupleRepository, FurnitureRepository furnitureRepository) {
        this.roomRepository = roomRepository;
        this.coupleRepository = coupleRepository;
        this.furnitureRepository = furnitureRepository;
    }

    public void createRoom(Long coupleId) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 커플을 찾을 수 없습니다."));

        Room room = new Room(couple);
        roomRepository.save(room);
    }

    public void decorateRoom(RoomDecorationRequestDTO request) {
        Room room = roomRepository.findByCouple_Id(request.getCoupleId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        Furniture furniture = furnitureRepository.findById(request.getFurnitureId())
                .orElseThrow(() -> new IllegalArgumentException("가구를 찾을 수 없습니다."));

        // 가구 배치 중복 확인 (예: 동일한 위치에 가구가 있는지)
        for (FurnitureLayout existingLayout : room.getFurnitureLayouts()) {
            if (existingLayout.getPositionX() == request.getPositionX() &&
                    existingLayout.getPositionY() == request.getPositionY()) {
                throw new IllegalArgumentException("해당 위치에 이미 가구가 배치되어 있습니다.");
            }
        }

        // 가구 배치 추가
        FurnitureLayout layout = new FurnitureLayout(furniture, request.getPositionX(), request.getPositionY(), request.getRotation());
        room.addFurnitureLayout(layout);

        roomRepository.save(room);
    }

    public RoomResponseDTO getRoomByCoupleId(Long coupleId) {
        Room room = roomRepository.findByCouple_Id(coupleId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        List<RoomResponseDTO.FurnitureLayoutDTO> furnitureLayouts = room.getFurnitureLayouts().stream()
                .map(layout -> new RoomResponseDTO.FurnitureLayoutDTO(
                        layout.getFurniture().getId(),
                        layout.getFurniture().getName(),
                        layout.getPositionX(),
                        layout.getPositionY(),
                        layout.getRotation()
                ))
                .toList();

        return new RoomResponseDTO(room.getId(), room.getCouple().getId(), furnitureLayouts);
    }
}
