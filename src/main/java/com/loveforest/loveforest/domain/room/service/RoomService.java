package com.loveforest.loveforest.domain.room.service;


import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.room.dto.*;
import com.loveforest.loveforest.domain.room.entity.Furniture;
import com.loveforest.loveforest.domain.room.entity.FurnitureLayout;
import com.loveforest.loveforest.domain.room.entity.Room;
import com.loveforest.loveforest.domain.room.exception.*;
import com.loveforest.loveforest.domain.room.repository.FurnitureLayoutRepository;
import com.loveforest.loveforest.domain.room.repository.FurnitureRepository;
import com.loveforest.loveforest.domain.room.repository.RoomRepository;
import com.loveforest.loveforest.domain.user.repository.UserInventoryRepository;
import com.loveforest.loveforest.exception.CustomException;
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
    private final UserInventoryRepository userInventoryRepository;

    /**
     * 커플의 방에 가구를 배치하는 메서드
     *
     * @param couple 커플룸 생성 시 필요
     * @throws RoomAlreadyExistsException 방 중복 생성
     * @throws RoomCreationFailedException 방 생성 중 시스템 오류
     * @throws InvalidRoomCreationException 잘못된 방 생성 요청
     */

    @Transactional
    public void createRoom(Couple couple) {
        try {
            // 1. 입력값 검증
            if (couple == null) {
                throw new InvalidRoomCreationException();
            }

            // 2. 기존 방 존재 여부 확인
            if (roomRepository.findByCoupleId(couple.getId()).isPresent()) {
                log.warn("방 생성 실패: 이미 존재하는 방입니다. CoupleId: {}", couple.getId());
                throw new RoomAlreadyExistsException();
            }

            // 3. 새로운 방 생성
            Room newRoom = new Room(couple);

            // 4. 방 저장 및 반환
            Room savedRoom = roomRepository.save(newRoom);
            log.info("새로운 방 생성 완료. RoomId: {}, CoupleId: {}", savedRoom.getId(), couple.getId());

        } catch (Exception e) {
            // CustomException을 제외한 예상치 못한 예외 처리
            if (!(e instanceof CustomException)) {
                log.error("방 생성 중 예상치 못한 오류 발생", e);
                throw new RoomCreationFailedException();
            }
            throw e;
        }
    }

    /**
     * 커플의 방에 가구를 배치하는 메서드
     *
     * @param request 가구 배치 요청 정보(커플 ID, 가구 ID, 위치 좌표, 회전 각도 등)
     * @throws RoomNotFoundException 방을 찾을 수 없는 경우 발생
     * @throws FurnitureNotFoundException 가구를 찾을 수 없는 경우 발생
     * @throws FurnitureOverlapException 가구가 겹치는 경우 발생
     */
    @Transactional
    public RoomDecorationResponseDTO decorateRoom(RoomDecorationRequestDTO request, Long coupleId) {
        Room room = roomRepository.findByCoupleId(coupleId)
                .orElseThrow(RoomNotFoundException::new);

        Furniture furniture = furnitureRepository.findById(request.getFurnitureId())
                .orElseThrow(FurnitureNotFoundException::new);

        // 3. 인벤토리에서 해당 가구 보유 여부 확인
        validateFurnitureOwnership(coupleId, request.getFurnitureId());

        // 4. 가구 배치 위치 검증
        FurnitureLayout newLayout = new FurnitureLayout(
                furniture,
                request.getPositionX(),
                request.getPositionY(),
                request.getRotation()
        );

        // 5. 가구 배치
        room.addFurnitureLayout(newLayout);
        Room savedRoom = roomRepository.save(room);

        log.info("가구 배치 완료 - coupleId: {}, furnitureId: {}, position: ({}, {})",
                coupleId, furniture.getId(), request.getPositionX(), request.getPositionY());
        // 6. 저장된 레이아웃 조회 (가장 최근에 추가된 레이아웃)
        FurnitureLayout savedLayout = savedRoom.getFurnitureLayouts()
                .get(savedRoom.getFurnitureLayouts().size() - 1);

        // 7. 응답 DTO 생성 및 반환
        return RoomDecorationResponseDTO.builder()
                .layoutId(savedLayout.getId())
                .furnitureId(furniture.getId())
                .furnitureName(furniture.getName())
                .positionX(savedLayout.getPositionX())
                .positionY(savedLayout.getPositionY())
                .rotation(savedLayout.getRotation())
                .width(furniture.getWidth())
                .height(furniture.getHeight())
                .build();
    }

    private void validateFurnitureOwnership(Long coupleId, Long furnitureId) {
        boolean hasItem = userInventoryRepository.existsByUser_Couple_IdAndShopItem_Furniture_Id(
                coupleId,
                furnitureId
        );

        if (!hasItem) {
            log.error("가구 보유 확인 실패 - coupleId: {}, furnitureId: {}", coupleId, furnitureId);
            throw new FurnitureNotInInventoryException();
        }
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
        Room room = roomRepository.findByCoupleId(coupleId)
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

        RoomResponseDTO.FloorDTO floorDTO = null;
        if (room.getFloor() != null) {
            floorDTO = new RoomResponseDTO.FloorDTO(
                    room.getFloor().getId(),
                    room.getFloor().getName(),
                    room.getFloor().getFloorNumber()
            );
        }

        RoomResponseDTO.WallpaperDTO wallpaperDTO = null;
        if (room.getWallpaper() != null) {
            wallpaperDTO = new RoomResponseDTO.WallpaperDTO(
                    room.getWallpaper().getId(),
                    room.getWallpaper().getName(),
                    room.getWallpaper().getWallpaperNumber()
            );
        }

        return new RoomResponseDTO(room.getId(), room.getCouple().getId(), furnitureLayouts, floorDTO, wallpaperDTO);
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
        Room room = roomRepository.findByCoupleId(coupleId)
                .orElseThrow(RoomNotFoundException::new);

        List<PublicRoomResponseDTO.PublicFurnitureDTO> furnitureLayouts = room.getFurnitureLayouts().stream()
                .map(layout -> new PublicRoomResponseDTO.PublicFurnitureDTO(
                        layout.getId(),
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
