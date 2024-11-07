package com.loveforest.loveforest.domain.room.service;


import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.room.dto.RoomDecorationRequestDTO;
import com.loveforest.loveforest.domain.room.dto.RoomFurnitureUpdateRequestDTO;
import com.loveforest.loveforest.domain.room.dto.RoomResponseDTO;
import com.loveforest.loveforest.domain.room.entity.Furniture;
import com.loveforest.loveforest.domain.room.entity.FurnitureLayout;
import com.loveforest.loveforest.domain.room.entity.Room;
import com.loveforest.loveforest.domain.room.exception.FurnitureLayoutNotFoundException;
import com.loveforest.loveforest.domain.room.exception.FurnitureNotFoundException;
import com.loveforest.loveforest.domain.room.exception.FurnitureOverlapException;
import com.loveforest.loveforest.domain.room.exception.RoomNotFoundException;
import com.loveforest.loveforest.domain.room.repository.FurnitureLayoutRepository;
import com.loveforest.loveforest.domain.room.repository.FurnitureRepository;
import com.loveforest.loveforest.domain.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final CoupleRepository coupleRepository;
    private final FurnitureRepository furnitureRepository;
    private final FurnitureLayoutRepository furnitureLayoutRepository;



    public void createRoom(Long coupleId) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 커플을 찾을 수 없습니다."));
        Room room = new Room(couple);
        roomRepository.save(room);
    }

    @Transactional
    public void decorateRoom(RoomDecorationRequestDTO request) {
        Room room = roomRepository.findByCouple_Id(request.getCoupleId())
                .orElseThrow(RoomNotFoundException::new);

        Furniture furniture = furnitureRepository.findById(request.getFurnitureId())
                .orElseThrow(FurnitureNotFoundException::new);

        // 가구 배치 중복 확인 (겹치는 영역이 있는지)
        for (FurnitureLayout existingLayout : room.getFurnitureLayouts()) {
            if (isOverlap(existingLayout, request.getPositionX(), request.getPositionY(), furniture.getWidth(), furniture.getHeight())) {
                throw new FurnitureOverlapException();
            }
        }

        // 가구 배치 추가
        FurnitureLayout layout = new FurnitureLayout(furniture, request.getPositionX(), request.getPositionY(), request.getRotation());
        room.addFurnitureLayout(layout);
        roomRepository.save(room);
    }

    private boolean isOverlap(FurnitureLayout layout, int posX, int posY, int width, int height) {
        int layoutRight = layout.getPositionX() + layout.getFurniture().getWidth();
        int layoutBottom = layout.getPositionY() + layout.getFurniture().getHeight();
        int newFurnitureRight = posX + width;
        int newFurnitureBottom = posY + height;

        return !(newFurnitureRight <= layout.getPositionX() || posX >= layoutRight ||
                newFurnitureBottom <= layout.getPositionY() || posY >= layoutBottom);
    }

    public RoomResponseDTO getRoomByCoupleId(Long coupleId) {
        Room room = roomRepository.findByCouple_Id(coupleId)
                .orElseThrow(RoomNotFoundException::new);

        List<RoomResponseDTO.FurnitureLayoutDTO> furnitureLayouts = room.getFurnitureLayouts().stream()
                .map(layout -> new RoomResponseDTO.FurnitureLayoutDTO(
                        layout.getFurniture().getId(),
                        layout.getFurniture().getName(),
                        layout.getPositionX(),
                        layout.getPositionY(),
                        layout.getRotation(),
                        layout.getFurniture().getWidth(),
                        layout.getFurniture().getHeight()
                ))
                .toList();

        return new RoomResponseDTO(room.getId(), room.getCouple().getId(), furnitureLayouts);
    }

    @Transactional
    public void moveFurniture(Long furnitureLayoutId, RoomFurnitureUpdateRequestDTO request) {
        FurnitureLayout layout = furnitureLayoutRepository.findById(furnitureLayoutId)
                .orElseThrow(FurnitureLayoutNotFoundException::new);

        layout.setPosition(request.getPositionX(), request.getPositionY(), request.getRotation());
        furnitureLayoutRepository.save(layout);
    }

    @Transactional
    public void deleteFurniture(Long furnitureLayoutId) {
        FurnitureLayout layout = furnitureLayoutRepository.findById(furnitureLayoutId)
                .orElseThrow(FurnitureLayoutNotFoundException::new);

        furnitureLayoutRepository.delete(layout);
    }
}
