package com.loveforest.loveforest.domain.room.service;


import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.room.dto.PublicRoomResponseDTO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final FurnitureRepository furnitureRepository;
    private final FurnitureLayoutRepository furnitureLayoutRepository;


    /**
     * 커플의 방에 가구를 배치하는 메서드
     *
     * @param request 가구 배치 요청 정보(커플 ID, 가구 ID, 위치 좌표, 회전 각도 등)
     * @throws RoomNotFoundException 방을 찾을 수 없는 경우 발생
     * @throws FurnitureNotFoundException 가구를 찾을 수 없는 경우 발생
     * @throws FurnitureOverlapException 가구가 겹치는 경우 발생
     */
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

    /**
     * 가구 배치 시 겹침 여부를 확인하는 내부 메서드
     *
     * @param layout 기존에 배치된 가구 레이아웃
     * @param posX 새로 배치할 X좌표
     * @param posY 새로 배치할 Y좌표
     * @param width 새로 배치할 가구의 너비
     * @param height 새로 배치할 가구의 높이
     * @return 가구가 겹치는 경우 true, 그렇지 않은 경우 false
     */
    private boolean isOverlap(FurnitureLayout layout, int posX, int posY, int width, int height) {
        int layoutRight = layout.getPositionX() + layout.getFurniture().getWidth();
        int layoutBottom = layout.getPositionY() + layout.getFurniture().getHeight();
        int newFurnitureRight = posX + width;
        int newFurnitureBottom = posY + height;

        return !(newFurnitureRight <= layout.getPositionX() || posX >= layoutRight ||
                newFurnitureBottom <= layout.getPositionY() || posY >= layoutBottom);
    }

    /**
     * 특정 커플의 방 정보를 조회하는 메서드
     *
     * @param coupleId 조회할 커플의 ID
     * @return 방 정보와 배치된 가구 목록이 포함된 RoomResponseDTO
     * @throws RoomNotFoundException 방을 찾을 수 없는 경우 발생
     */
    public RoomResponseDTO getRoomByCoupleId(Long coupleId) {
        Room room = roomRepository.findByCouple_Id(coupleId)
                .orElseThrow(RoomNotFoundException::new);

        List<RoomResponseDTO.FurnitureLayoutDTO> furnitureLayouts = room.getFurnitureLayouts().stream()
                .map(layout -> new RoomResponseDTO.FurnitureLayoutDTO(
                        layout.getId(),
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

    /**
     * 배치된 가구의 위치를 이동하는 메서드
     *
     * @param furnitureLayoutId 이동할 가구 레이아웃의 ID
     * @param request 새로운 위치 정보(X좌표, Y좌표, 회전 각도)
     * @throws FurnitureLayoutNotFoundException 가구 레이아웃을 찾을 수 없는 경우 발생
     */
    @Transactional
    public void moveFurniture(Long furnitureLayoutId, RoomFurnitureUpdateRequestDTO request) {
        FurnitureLayout layout = furnitureLayoutRepository.findById(furnitureLayoutId)
                .orElseThrow(FurnitureLayoutNotFoundException::new);

        layout.setPosition(request.getPositionX(), request.getPositionY(), request.getRotation());
        furnitureLayoutRepository.save(layout);
    }

    /**
     * 배치된 가구를 삭제하는 메서드
     *
     * @param furnitureLayoutId 삭제할 가구 레이아웃의 ID
     * @throws FurnitureLayoutNotFoundException 가구 레이아웃을 찾을 수 없는 경우 발생
     */
    @Transactional
    public void deleteFurniture(Long furnitureLayoutId) {
        // 가구 레이아웃 존재 여부 확인
        FurnitureLayout layout = furnitureLayoutRepository.findById(furnitureLayoutId)
                .orElseThrow(FurnitureLayoutNotFoundException::new);

        // Room에서도 해당 가구 레이아웃 제거 (영속성 전파를 위해)
        Room room = layout.getRoom();
        room.removeFurnitureLayout(layout);

        // 저장소에서 가구 레이아웃 삭제
        furnitureLayoutRepository.delete(layout);
        roomRepository.save(room);  // Room 엔티티 업데이트
    }

    /**
     * 다른 사용자의 방 정보를 공개적으로 조회하는 메서드
     * 커플의 방 정보 중 공개 가능한 정보만 제공
     *
     * @param coupleId 조회할 커플의 ID
     * @return 공개 가능한 방 정보가 포함된 PublicRoomResponseDTO
     * @throws RoomNotFoundException 방을 찾을 수 없는 경우 발생
     */
    @Transactional(readOnly = true)
    public PublicRoomResponseDTO getPublicRoomByCoupleId(Long coupleId) {
        Room room = roomRepository.findByCouple_Id(coupleId)
                .orElseThrow(RoomNotFoundException::new);

        List<PublicRoomResponseDTO.PublicFurnitureDTO> furnitureLayouts = room.getFurnitureLayouts().stream()
                .map(layout -> new PublicRoomResponseDTO.PublicFurnitureDTO(
                        layout.getFurniture().getId(),
                        layout.getFurniture().getName(),
                        layout.getPositionX(),
                        layout.getPositionY(),
                        layout.getRotation()
                ))
                .toList();

        return new PublicRoomResponseDTO(room.getId(), room.getCouple().getId(), furnitureLayouts);
    }
}
