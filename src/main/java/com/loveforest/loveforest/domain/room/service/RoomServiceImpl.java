package com.loveforest.loveforest.domain.room.service;


import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.exception.CoupleNotFoundException;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.room.dto.*;
import com.loveforest.loveforest.domain.room.entity.*;
import com.loveforest.loveforest.domain.room.exception.*;
import com.loveforest.loveforest.domain.room.repository.*;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.repository.UserInventoryRepository;
import com.loveforest.loveforest.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 통합된 방 데코레이션 서비스 구현체
 * SRP를 지키면서도 연관된 책임들을 하나의 클래스에서 관리
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final FurnitureRepository furnitureRepository;
    private final WallpaperRepository wallpaperRepository;
    private final FloorRepository floorRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final FurnitureLayoutRepository furnitureLayoutRepository;
    private final CoupleRepository coupleRepository;


    @Override
    @Transactional
    public RoomDecorationResponseDTO placeFurniture(Long coupleId, RoomDecorationRequestDTO request) {
        Room room = findRoomByCouple(coupleId);
        Furniture furniture = validateAndGetFurniture(coupleId, request.getFurnitureId());

        validateFurniturePosition(room, request);

        // 1. FurnitureLayout을 먼저 저장하여 ID 생성
        FurnitureLayout newLayout = createFurnitureLayout(furniture, request);
        FurnitureLayout savedLayout = furnitureLayoutRepository.save(newLayout);

        // 2. 저장된 Layout을 Room에 추가
        room.addFurnitureLayout(savedLayout);
        Room savedRoom = roomRepository.save(room);
        return createDecorationResponse(savedRoom, newLayout);
    }

    @Override
    @Transactional
    public RoomResponseDTO setWallpaper(Long coupleId, Long wallpaperId) {
        Room room = findRoomByCouple(coupleId);
        Wallpaper wallpaper = validateAndGetWallpaper(coupleId, wallpaperId);

        room.setWallpaper(wallpaper);
        Room savedRoom = roomRepository.save(room);
        log.info("벽지 설정 완료 - coupleId: {}, wallpaperId: {}", coupleId, wallpaperId);
        return createRoomResponse(savedRoom);
    }


    @Override
    @Transactional
    public RoomResponseDTO setFloor(Long coupleId, Long floorId) {
        Room room = findRoomByCouple(coupleId);
        Floor floor = validateAndGetFloor(coupleId, floorId);

        room.setFloor(floor);
        Room savedRoom = roomRepository.save(room);

        log.info("바닥 설정 완료 - coupleId: {}, floorId: {}", coupleId, floorId);

        return createRoomResponse(savedRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomStatus(Long coupleId) {
        Room room = findRoomByCouple(coupleId);
        return createRoomResponse(room);
    }

    private Room findRoomByCouple(Long coupleId) {
        return roomRepository.findByCoupleId(coupleId)
                .orElseThrow(RoomNotFoundException::new);
    }

    private Furniture validateAndGetFurniture(Long coupleId, Long furnitureId) {
        if (!userInventoryRepository.existsByUser_Couple_IdAndShopItem_Furniture_Id(coupleId, furnitureId)) {
            throw new FurnitureNotInInventoryException();
        }
        return furnitureRepository.findById(furnitureId)
                .orElseThrow(FurnitureNotFoundException::new);
    }

    private void validateFurniturePosition(Room room, RoomDecorationRequestDTO request) {
        // 가구 위치 중복 검사 로직
        boolean isOverlapping = room.getFurnitureLayouts().stream()
                .anyMatch(layout -> isFurnitureOverlapping(layout, request));
        if (isOverlapping) {
            throw new FurnitureOverlapException();
        }
    }

    private boolean isFurnitureOverlapping(FurnitureLayout existing, RoomDecorationRequestDTO request) {
        // 가구 충돌 감지 로직 구현
        int existingRight = existing.getPositionX() + existing.getFurniture().getWidth();
        int existingBottom = existing.getPositionY() + existing.getFurniture().getHeight();
        int newRight = request.getPositionX() + request.getWidth();
        int newBottom = request.getPositionY() + request.getHeight();

        return !(newRight <= existing.getPositionX() ||
                request.getPositionX() >= existingRight ||
                newBottom <= existing.getPositionY() ||
                request.getPositionY() >= existingBottom);
    }

    private RoomDecorationResponseDTO createDecorationResponse(Room room, FurnitureLayout layout) {
        return RoomDecorationResponseDTO.builder()
                .layoutId(layout.getId())
                .furnitureId(layout.getFurniture().getId())
                .furnitureName(layout.getFurniture().getName())
                .positionX(layout.getPositionX())
                .positionY(layout.getPositionY())
                .rotation(layout.getRotation())
                .width(layout.getFurniture().getWidth())
                .height(layout.getFurniture().getHeight())
                .build();
    }

    @Override
    @Transactional
    public RoomDecorationResponseDTO moveFurniture(Long furnitureLayoutId, RoomFurnitureUpdateRequestDTO request) {
        FurnitureLayout layout = furnitureLayoutRepository.findById(furnitureLayoutId)
                .orElseThrow(FurnitureLayoutNotFoundException::new);

        // 이동할 위치에 다른 가구가 있는지 확인
        Room room = layout.getRoom();
        boolean isOverlapping = room.getFurnitureLayouts().stream()
                .filter(existingLayout -> !existingLayout.getId().equals(furnitureLayoutId))
                .anyMatch(existingLayout -> isFurnitureOverlapping(existingLayout, request));

        if (isOverlapping) {
            throw new FurnitureOverlapException();
        }

        layout.setPosition(request.getPositionX(), request.getPositionY(), request.getRotation());
        furnitureLayoutRepository.save(layout);
        log.info("가구 위치 이동 완료 - layoutId: {}, 새 위치: ({}, {})",
                furnitureLayoutId, request.getPositionX(), request.getPositionY());
        return new RoomDecorationResponseDTO(
                layout.getId(),
                layout.getFurniture().getId(),
                layout.getFurniture().getName(),
                layout.getPositionX(),
                layout.getPositionY(),
                layout.getRotation(),
                layout.getFurniture().getWidth(),
                layout.getFurniture().getHeight()
        );
    }

    @Override
    @Transactional
    public RoomResponseDTO removeFurniture(Long furnitureLayoutId) {
        FurnitureLayout layout = furnitureLayoutRepository.findById(furnitureLayoutId)
                .orElseThrow(FurnitureLayoutNotFoundException::new);

        Room room = layout.getRoom();
        room.removeFurnitureLayout(layout);
        Room savedRoom = roomRepository.save(room);

        // 가구 레이아웃 삭제
        furnitureLayoutRepository.delete(layout);
        log.info("가구 제거 완료 - layoutId: {}", furnitureLayoutId);
        return createRoomResponse(savedRoom);
    }

    @Override
    @Transactional
    public RoomResponseDTO removeWallpaper(Long coupleId) {
        Room room = findRoomByCouple(coupleId);

        // 이전 벽지 정보 로깅
        String previousWallpaper = room.getWallpaper() != null ?
                room.getWallpaper().getName() : "없음";

        room.setWallpaper(null);
        Room savedRoom = roomRepository.save(room);
        log.info("벽지 제거 완료 - coupleId: {}, 이전 벽지: {}", coupleId, previousWallpaper);
        return createRoomResponse(savedRoom);
    }

    @Override
    @Transactional
    public RoomResponseDTO removeFloor(Long coupleId) {
        Room room = findRoomByCouple(coupleId);

        // 이전 바닥 정보 로깅
        String previousFloor = room.getFloor() != null ?
                room.getFloor().getName() : "없음";

        room.setFloor(null);
        Room savedRoom = roomRepository.save(room);
        log.info("바닥 제거 완료 - coupleId: {}, 이전 바닥: {}", coupleId, previousFloor);
        return createRoomResponse(savedRoom);
    }

    private RoomResponseDTO createRoomResponse(Room room) {
        // 가구 레이아웃 변환
        List<RoomResponseDTO.FurnitureLayoutDTO> furnitureLayouts = room.getFurnitureLayouts()
                .stream()
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

        // 벽지 정보 변환
        RoomResponseDTO.WallpaperDTO wallpaperDTO = null;
        if (room.getWallpaper() != null) {
            wallpaperDTO = new RoomResponseDTO.WallpaperDTO(
                    room.getWallpaper().getId(),
                    room.getWallpaper().getName(),
                    room.getWallpaper().getWallpaperNumber()
            );
        }

        // 바닥 정보 변환
        RoomResponseDTO.FloorDTO floorDTO = null;
        if (room.getFloor() != null) {
            floorDTO = new RoomResponseDTO.FloorDTO(
                    room.getFloor().getId(),
                    room.getFloor().getName(),
                    room.getFloor().getFloorNumber()
            );
        }

        return new RoomResponseDTO(
                room.getId(),
                room.getCouple().getId(),
                furnitureLayouts,
                floorDTO,
                wallpaperDTO
        );
    }

    private Wallpaper validateAndGetWallpaper(Long coupleId, Long wallpaperId) {
        if (!userInventoryRepository.existsByUser_Couple_IdAndShopItem_Wallpaper_Id(coupleId, wallpaperId)) {
            log.error("벽지 보유 확인 실패 - coupleId: {}, wallpaperId: {}", coupleId, wallpaperId);
            throw new WallpaperNotInInventoryException();
        }
        return wallpaperRepository.findById(wallpaperId)
                .orElseThrow(WallpaperNotFoundException::new);
    }

    private Floor validateAndGetFloor(Long coupleId, Long floorId) {
        if (!userInventoryRepository.existsByUser_Couple_IdAndShopItem_Floor_Id(coupleId, floorId)) {
            log.error("바닥 보유 확인 실패 - coupleId: {}, floorId: {}", coupleId, floorId);
            throw new FloorNotInInventoryException();
        }
        return floorRepository.findById(floorId)
                .orElseThrow(FloorNotFoundException::new);
    }

    private FurnitureLayout createFurnitureLayout(Furniture furniture, RoomDecorationRequestDTO request) {
        return new FurnitureLayout(
                furniture,
                request.getPositionX(),
                request.getPositionY(),
                request.getRotation()
        );
    }

    // 가구 충돌 검사를 위해 메서드 시그니처 수정
    private boolean isFurnitureOverlapping(FurnitureLayout existing, RoomFurnitureUpdateRequestDTO request) {
        return isOverlapping(
                existing.getPositionX(),
                existing.getPositionY(),
                existing.getFurniture().getWidth(),
                existing.getFurniture().getHeight(),
                request.getPositionX(),
                request.getPositionY(),
                existing.getFurniture().getWidth(),
                existing.getFurniture().getHeight()
        );
    }

    // 가구 충돌 검사 공통 로직
    private boolean isOverlapping(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return !(x2 >= x1 + w1 ||    // 두 번째 가구가 첫 번째 가구의 오른쪽에 있음
                x2 + w2 <= x1 ||     // 두 번째 가구가 첫 번째 가구의 왼쪽에 있음
                y2 >= y1 + h1 ||     // 두 번째 가구가 첫 번째 가구의 위에 있음
                y2 + h2 <= y1);      // 두 번째 가구가 첫 번째 가구의 아래에 있음
    }

    @Override
    @Transactional(readOnly = true)
    public PublicRoomResponseDTO getPublicRoomInfo(Long coupleId, Long requesterId) {
        // 자신의 방을 조회하는 경우 예외 처리
        if (isRequestingOwnRoom(coupleId, requesterId)) {
            throw new InvalidRoomAccessException("자신의 방은 일반 조회 API를 사용해주세요.");
        }

        Room room = roomRepository.findByCoupleId(coupleId)
                .orElseThrow(RoomNotFoundException::new);

        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(CoupleNotFoundException::new);

        // 가구 정보 변환
        List<PublicRoomResponseDTO.PublicFurnitureDTO> furnitureLayouts = room.getFurnitureLayouts()
                .stream()
                .map(this::convertToPublicFurnitureDTO)
                .collect(Collectors.toList());

        // 방 스타일 정보 생성
        PublicRoomResponseDTO.RoomStyleDTO styleDTO = PublicRoomResponseDTO.RoomStyleDTO.builder()
                .wallpaperName(room.getWallpaper() != null ? room.getWallpaper().getName() : null)
                .floorName(room.getFloor() != null ? room.getFloor().getName() : null)
                .build();

        // 커플 이름 생성 (두 사용자의 닉네임 조합)
        String coupleName = generateCoupleName(couple);

        return PublicRoomResponseDTO.builder()
                .roomId(room.getId())
                .coupleId(coupleId)
                .coupleName(coupleName)
                .style(styleDTO)
                .furnitureLayouts(furnitureLayouts)
                .build();
    }

    private PublicRoomResponseDTO.PublicFurnitureDTO convertToPublicFurnitureDTO(FurnitureLayout layout) {
        return PublicRoomResponseDTO.PublicFurnitureDTO.builder()
                .furnitureId(layout.getFurniture().getId())
                .furnitureName(layout.getFurniture().getName())
                .positionX(layout.getPositionX())
                .positionY(layout.getPositionY())
                .rotation(layout.getRotation())
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

    private boolean isRequestingOwnRoom(Long coupleId, Long requesterId) {
        return coupleRepository.findById(coupleId)
                .map(couple -> couple.getUsers().stream()
                        .anyMatch(user -> user.getId().equals(requesterId)))
                .orElse(false);
    }
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

}
