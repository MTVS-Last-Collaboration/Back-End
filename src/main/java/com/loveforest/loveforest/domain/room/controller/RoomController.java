package com.loveforest.loveforest.domain.room.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.room.dto.*;
import com.loveforest.loveforest.domain.room.service.PresetRoomService;
import com.loveforest.loveforest.domain.room.service.RoomCollectionService;
import com.loveforest.loveforest.domain.room.service.RoomServiceImpl;
import com.loveforest.loveforest.domain.room.service.SharedRoomService;
import com.loveforest.loveforest.domain.user.exception.LoginRequiredException;
import com.loveforest.loveforest.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "방 꾸미기 API", description = "방 꾸미기 관련 API 입니다.")
public class RoomController {

    private final RoomServiceImpl roomServiceImpl;
    private final RoomCollectionService collectionService;
    private final SharedRoomService sharedRoomService;
    private final PresetRoomService presetRoomService;

    /**
     * 방 가구 배치 API
     */
    @Operation(
            summary = "가구 배치",
            description = "방에 새로운 가구를 배치합니다. 보유한 가구만 배치할 수 있으며, 다른 가구와 겹치지 않아야 합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "가구 배치 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RoomDecorationApiResponseDTO.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "message": "가구 배치가 완료되었습니다.",
                                        "data": {
                                            "layoutId": 1,
                                            "furnitureId": 2,
                                            "furnitureName": "클래식 소파",
                                            "positionX": 100,
                                            "positionY": 200,
                                            "rotation": 90,
                                            "width": 2,
                                            "height": 3
                                        }
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (가구 미보유 또는 잘못된 위치)",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "가구 배치 충돌",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @PostMapping("/furniture")
    public ResponseEntity<RoomDecorationApiResponseDTO> placeFurniture(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @Valid @RequestBody RoomDecorationRequestDTO request) {
        validateLogin(loginInfo);

        log.info("가구 배치 요청 - 커플 ID: {}, 가구 ID: {}", loginInfo.getCoupleId(), request.getFurnitureId());
        RoomDecorationResponseDTO response = roomServiceImpl.placeFurniture(loginInfo.getCoupleId(), request);

        return ResponseEntity.ok(new RoomDecorationApiResponseDTO(response));
    }


    /**
     * 방 가구 이동 API
     */
    @Operation(
            summary = "가구 위치 이동",
            description = "배치된 가구의 위치와 회전을 변경하고, 변경된 가구의 상세 정보를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "가구 이동 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RoomDecorationApiResponseDTO.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "message": "가구 이동이 완료되었습니다.",
                                        "data": {
                                            "layoutId": 1,
                                            "furnitureId": 2,
                                            "furnitureName": "클래식 소파",
                                            "positionX": 150,
                                            "positionY": 250,
                                            "rotation": 180,
                                            "width": 2,
                                            "height": 3
                                        }
                                    }
                                    """)
                            )
                    )
            }
    )
    @PutMapping("/furniture/{furnitureLayoutId}")
    public ResponseEntity<SimpleApiResponseDTO> moveFurniture(@AuthenticationPrincipal LoginInfo loginInfo, @PathVariable("furnitureLayoutId") Long furnitureLayoutId,
            @Valid @RequestBody RoomFurnitureUpdateRequestDTO request) {
        validateLogin(loginInfo);

        log.info("가구 이동 요청 - 가구 레이아웃 ID: {}, 새 위치: ({}, {})",
                furnitureLayoutId, request.getPositionX(), request.getPositionY());
        roomServiceImpl.moveFurniture(furnitureLayoutId, request);

        return ResponseEntity.ok(new SimpleApiResponseDTO("가구 이동이 완료되었습니다."));
    }

    /**
     * 방 가구 제거 API
     */
    @Operation(
            summary = "가구 제거",
            description = "배치된 가구를 제거하고 변경된 방의 전체 상태를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "가구 제거 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RoomStatusApiResponseDTO.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "message": "가구가 성공적으로 제거되었습니다.",
                                        "data": {
                                            "roomId": 1,
                                            "coupleId": 1,
                                            "furnitureLayouts": [
                                                {
                                                    "layoutId": 2,
                                                    "furnitureId": 3,
                                                    "furnitureName": "우아한 의자",
                                                    "positionX": 300,
                                                    "positionY": 400,
                                                    "rotation": 0,
                                                    "width": 1,
                                                    "height": 1
                                                }
                                            ],
                                            "wallpaper": {
                                                "id": 1,
                                                "name": "클래식 벽지",
                                                "wallpaperNumber": 1
                                            },
                                            "floor": {
                                                "id": 1,
                                                "name": "원목 바닥",
                                                "floorNumber": 1
                                            }
                                        }
                                    }
                                    """)
                            )
                    )
            }
    )
    @DeleteMapping("/furniture/{furnitureLayoutId}")
    public ResponseEntity<SimpleApiResponseDTO> removeFurniture(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @PathVariable("furnitureLayoutId") Long furnitureLayoutId) {
        validateLogin(loginInfo);

        log.info("가구 제거 요청 - 가구 레이아웃 ID: {}", furnitureLayoutId);
        roomServiceImpl.removeFurniture(furnitureLayoutId);

        return ResponseEntity.ok(new SimpleApiResponseDTO("가구가 성공적으로 제거되었습니다."));
    }


    /**
     * 방 벽지 설정 API
     */
    @Operation(
            summary = "벽지 설정",
            description = "방의 벽지를 변경하고, 변경된 방의 전체 상태를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "벽지 설정 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RoomStatusApiResponseDTO.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "message": "벽지 설정이 완료되었습니다.",
                                        "data": {
                                            "roomId": 1,
                                            "coupleId": 1,
                                            "furnitureLayouts": [
                                                {
                                                    "layoutId": 1,
                                                    "furnitureId": 2,
                                                    "furnitureName": "클래식 소파",
                                                    "positionX": 100,
                                                    "positionY": 200,
                                                    "rotation": 90,
                                                    "width": 2,
                                                    "height": 3
                                                }
                                            ],
                                            "wallpaper": {
                                                "id": 3,
                                                "name": "모던 벽지",
                                                "wallpaperNumber": 3
                                            },
                                            "floor": {
                                                "id": 1,
                                                "name": "원목 바닥",
                                                "floorNumber": 1
                                            }
                                        }
                                    }
                                    """)
                            )
                    )
            }
    )
    @PostMapping("/wallpaper/{wallpaperId}")
    public ResponseEntity<RoomStatusApiResponseDTO> setWallpaper(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @PathVariable("wallpaperId") Long wallpaperId) {
        validateLogin(loginInfo);

        log.info("벽지 설정 요청 - 커플 ID: {}, 벽지 ID: {}", loginInfo.getCoupleId(), wallpaperId);
        RoomResponseDTO response = roomServiceImpl.setWallpaper(loginInfo.getCoupleId(), wallpaperId);

        return ResponseEntity.ok(new RoomStatusApiResponseDTO("벽지 설정이 완료되었습니다.", response));
    }


    @Operation(
            summary = "벽지 제거",
            description = "현재 설정된 벽지를 제거하고, 변경된 방의 전체 상태를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "벽지 제거 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RoomStatusApiResponseDTO.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "message": "벽지 제거가 완료되었습니다.",
                                        "data": {
                                            "roomId": 1,
                                            "coupleId": 1,
                                            "furnitureLayouts": [
                                                {
                                                    "layoutId": 1,
                                                    "furnitureId": 2,
                                                    "furnitureName": "클래식 소파",
                                                    "positionX": 100,
                                                    "positionY": 200,
                                                    "rotation": 90,
                                                    "width": 2,
                                                    "height": 3
                                                }
                                            ],
                                            "wallpaper": null,
                                            "floor": {
                                                "id": 1,
                                                "name": "원목 바닥",
                                                "floorNumber": 1
                                            }
                                        }
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "방을 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @DeleteMapping("/wallpaper")
    public ResponseEntity<RoomStatusApiResponseDTO> removeWallpaper(@AuthenticationPrincipal LoginInfo loginInfo) {
        validateLogin(loginInfo);

        log.info("벽지 제거 요청 - 커플 ID: {}", loginInfo.getCoupleId());
        RoomResponseDTO response = roomServiceImpl.removeWallpaper(loginInfo.getCoupleId());

        return ResponseEntity.ok(new RoomStatusApiResponseDTO("벽지 제거가 완료되었습니다.", response));
    }

    /**
     * 방 바닥 설정 API
     */
    @Operation(
            summary = "바닥 설정",
            description = "방의 바닥을 변경하고, 변경된 방의 전체 상태를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "바닥 설정 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RoomStatusApiResponseDTO.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "message": "바닥 설정이 완료되었습니다.",
                                        "data": {
                                            "roomId": 1,
                                            "coupleId": 1,
                                            "furnitureLayouts": [
                                                {
                                                    "layoutId": 1,
                                                    "furnitureId": 2,
                                                    "furnitureName": "클래식 소파",
                                                    "positionX": 100,
                                                    "positionY": 200,
                                                    "rotation": 90,
                                                    "width": 2,
                                                    "height": 3
                                                }
                                            ],
                                            "wallpaper": {
                                                "id": 3,
                                                "name": "모던 벽지",
                                                "wallpaperNumber": 3
                                            },
                                            "floor": {
                                                "id": 2,
                                                "name": "대리석 바닥",
                                                "floorNumber": 2
                                            }
                                        }
                                    }
                                    """)
                            )
                    )
            }
    )
    @PostMapping("/floor/{floorId}")
    public ResponseEntity<RoomStatusApiResponseDTO> setFloor(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @PathVariable("floorId") Long floorId) {
        validateLogin(loginInfo);

        log.info("바닥 설정 요청 - 커플 ID: {}, 바닥 ID: {}", loginInfo.getCoupleId(), floorId);
        RoomResponseDTO response = roomServiceImpl.setFloor(loginInfo.getCoupleId(), floorId);

        return ResponseEntity.ok(new RoomStatusApiResponseDTO("바닥 설정이 완료되었습니다.", response));
    }

    /**
     * 방 바닥 제거 API
     */
    @Operation(
            summary = "바닥 제거",
            description = "현재 설정된 바닥을 제거하고, 변경된 방의 전체 상태를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "바닥 제거 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RoomStatusApiResponseDTO.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "message": "바닥 제거가 완료되었습니다.",
                                        "data": {
                                            "roomId": 1,
                                            "coupleId": 1,
                                            "furnitureLayouts": [
                                                {
                                                    "layoutId": 1,
                                                    "furnitureId": 2,
                                                    "furnitureName": "클래식 소파",
                                                    "positionX": 100,
                                                    "positionY": 200,
                                                    "rotation": 90,
                                                    "width": 2,
                                                    "height": 3
                                                }
                                            ],
                                            "wallpaper": {
                                                "id": 3,
                                                "name": "모던 벽지",
                                                "wallpaperNumber": 3
                                            },
                                            "floor": null
                                        }
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "방을 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @DeleteMapping("/floor")
    public ResponseEntity<RoomStatusApiResponseDTO> removeFloor(@AuthenticationPrincipal LoginInfo loginInfo) {
        validateLogin(loginInfo);

        log.info("바닥 제거 요청 - 커플 ID: {}", loginInfo.getCoupleId());
        RoomResponseDTO response = roomServiceImpl.removeFloor(loginInfo.getCoupleId());

        return ResponseEntity.ok(new RoomStatusApiResponseDTO("바닥 제거가 완료되었습니다.", response));
    }


    /**
     * 방 전체 상태 조회 API
     */
    @Operation(
            summary = "방 전체 상태 조회",
            description = "현재 방의 모든 데코레이션 상태(가구, 벽지, 바닥)를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RoomStatusApiResponseDTO.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "message": "방 상태 조회가 완료되었습니다.",
                                        "data": {
                                            "roomId": 1,
                                            "coupleId": 1,
                                            "furnitureLayouts": [
                                                {
                                                    "layoutId": 1,
                                                    "furnitureId": 2,
                                                    "furnitureName": "클래식 소파",
                                                    "positionX": 100,
                                                    "positionY": 200,
                                                    "rotation": 90,
                                                    "width": 2,
                                                    "height": 3
                                                },
                                                {
                                                    "layoutId": 2,
                                                    "furnitureId": 3,
                                                    "furnitureName": "우아한 의자",
                                                    "positionX": 300,
                                                    "positionY": 400,
                                                    "rotation": 0,
                                                    "width": 1,
                                                    "height": 1
                                                }
                                            ],
                                            "wallpaper": {
                                                "id": 3,
                                                "name": "모던 벽지",
                                                "wallpaperNumber": 3
                                            },
                                            "floor": {
                                                "id": 2,
                                                "name": "대리석 바닥",
                                                "floorNumber": 2
                                            }
                                        }
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "방을 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @GetMapping("/status")
    public ResponseEntity<RoomStatusApiResponseDTO> getRoomStatus(@AuthenticationPrincipal LoginInfo loginInfo) {
        validateLogin(loginInfo);

        log.info("방 상태 조회 요청 - 커플 ID: {}", loginInfo.getCoupleId());
        RoomResponseDTO response = roomServiceImpl.getRoomStatus(loginInfo.getCoupleId());

        return ResponseEntity.ok(new RoomStatusApiResponseDTO(response));
    }

    private void validateLogin(LoginInfo loginInfo) {
        if (loginInfo == null) {
            log.warn("인증되지 않은 사용자의 접근 시도");
            throw new LoginRequiredException();
        }
    }

    /**
     * 다른 커플의 방 조회 API
     */
    @Operation(
            summary = "다른 커플의 방 조회",
            description = """
                특정 커플의 방 정보를 조회합니다. 
                - 자신의 방은 이 API로 조회할 수 없습니다.
                - 공개 가능한 정보만 반환됩니다.
                - 가구 배치, 벽지, 바닥 등의 기본 정보가 포함됩니다.
                """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "방 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PublicRoomApiResponseDTO.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "message": "다른 커플의 방 조회가 완료되었습니다.",
                                        "data": {
                                            "roomId": 1,
                                            "coupleId": 1,
                                            "coupleName": "철수♥영희",
                                            "style": {
                                                "wallpaperName": "모던 벽지",
                                                "floorName": "대리석 바닥"
                                            },
                                            "furnitureLayouts": [
                                                {
                                                    "furnitureId": 2,
                                                    "furnitureName": "클래식 소파",
                                                    "positionX": 100,
                                                    "positionY": 200,
                                                    "rotation": 90
                                                },
                                                {
                                                    "furnitureId": 3,
                                                    "furnitureName": "우아한 의자",
                                                    "positionX": 300,
                                                    "positionY": 400,
                                                    "rotation": 0
                                                }
                                            ]
                                        }
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "자신의 방을 조회하려 할 때",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "방을 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @GetMapping("/public/{coupleId}")
    public ResponseEntity<PublicRoomApiResponseDTO> getPublicRoomInfo(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @Parameter(description = "조회할 커플의 ID", example = "1")
            @PathVariable("coupleId") Long coupleId) {
        validateLogin(loginInfo);

        log.info("다른 커플의 방 조회 요청 - 요청자: {}, 대상 커플: {}",
                loginInfo.getUserId(), coupleId);

        PublicRoomResponseDTO response = roomServiceImpl.getPublicRoomInfo(coupleId, loginInfo.getUserId());

        log.info("다른 커플의 방 조회 완료 - 커플: {}", coupleId);
        return ResponseEntity.ok(new PublicRoomApiResponseDTO(response));
    }

    /**
     * 현재 방 상태 저장 API
     */
    @Operation(
            summary = "현재 방 상태 저장",
            description = """
            현재 방의 상태를 컬렉션에 저장합니다. 저장된 방은 추후 다시 불러올 수 있으며, 
            방의 썸네일 이미지를 함께 업로드할 수 있습니다.
            썸네일 이미지는 선택적으로 제공 가능하며, 최대 크기와 지원 형식을 준수해야 합니다.
        """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "썸네일 이미지를 포함한 현재 방 상태 저장 요청",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = MultipartFile.class)
                    ),
                    required = false
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "현재 방 상태 저장 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RoomOperationResponseDTO.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "message": "방 상태가 성공적으로 저장되었습니다.",
                                        "timestamp": "2024-11-25T15:30:00",
                                        "data": {
                                            "savedAt": "2024-11-25T15:30:00",
                                            "thumbnailUrl": "https://s3.amazonaws.com/example/room_thumbnail.jpg"
                                        }
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "로그인 필요",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "status": 401,
                                        "errorType": "Unauthorized",
                                        "message": "로그인이 필요합니다.",
                                        "code": "USER-004"
                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                            잘못된 요청:
                            - 이미지가 지원되지 않는 형식일 경우
                            - 이미지 파일 크기가 초과된 경우
                        """,
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                    {
                                        "status": 400,
                                        "errorType": "Invalid Image Format",
                                        "message": "지원하지 않는 이미지 형식입니다.",
                                        "code": "ROOM-016"
                                    }
                                    """)
                            )
                    )
            }
    )
    @PostMapping("/collection/current")
    public ResponseEntity<RoomOperationResponseDTO> saveCurrentRoom(@AuthenticationPrincipal LoginInfo loginInfo,
                                                @RequestParam(value = "thumbnail",required = false) MultipartFile thumbnail) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("현재 방 상태 저장 요청 - 커플 ID: {}, 이미지 크기: {}",
                loginInfo.getCoupleId(),
                thumbnail != null ? thumbnail.getSize() : 0);
        RoomOperationResponseDTO response = collectionService.saveCurrentRoom(loginInfo.getCoupleId(), thumbnail);

        return ResponseEntity.ok(response);
    }

    /**
     * 프리셋 방 저장 API
     */
    @Operation(
            summary = "프리셋 방 저장",
            description = """
        선택한 방을 프리셋으로 저장합니다.
        - 프리셋 저장 시 썸네일 이미지를 함께 업로드할 수 있습니다.
        - 썸네일은 선택 사항이며, 최대 5MB 크기 제한과 JPEG/PNG 형식을 지원합니다.
    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "프리셋 방 저장 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PresetRoomResponseDTO.class),
                                    examples = @ExampleObject(value = """
                {
                    "presetId": 1,
                    "name": "클래식 룸",
                    "wallpaper": {
                        "id": 1,
                        "name": "모던 벽지",
                        "wallpaperNumber": 1
                    },
                    "floor": {
                        "id": 1,
                        "name": "원목 바닥",
                        "floorNumber": 1
                    },
                    "furnitureLayouts": [
                        {
                            "furnitureId": 1,
                            "name": "클래식 소파",
                            "positionX": 100,
                            "positionY": 200,
                            "rotation": 90
                        }
                    ],
                    "thumbnailUrl": "https://example.com/thumbnail.jpg"
                }
                """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (이미지 형식 오류, 크기 초과 등)",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증되지 않은 사용자",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @PostMapping(value = "/collection/preset/{presetId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RoomOperationResponseDTO> savePresetRoom(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @PathVariable("presetId") Long presetId,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("프리셋 방 저장 요청 - 커플 ID: {}, 프리셋 ID: {}, 이미지 여부: {}",
                loginInfo.getCoupleId(),
                presetId,
                thumbnail != null && !thumbnail.isEmpty());
        RoomOperationResponseDTO response = collectionService.savePresetRoom(
                loginInfo.getCoupleId(),
                presetId,
                thumbnail
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 공유된 방 저장 API
     */
    @Operation(
            summary = "공유된 방 저장",
            description = "공유된 다른 커플의 방을 컬렉션에 저장합니다. 방의 스크린샷도 함께 저장할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공유된 방 저장 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이미지 형식 오류, 크기 초과 등)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                {
                    "status": 400,
                    "errorType": "Invalid Image Format",
                    "message": "지원하지 않는 이미지 형식입니다.",
                    "code": "ROOM-016"
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                {
                    "status": 401,
                    "errorType": "Unauthorized",
                    "message": "로그인이 필요합니다.",
                    "code": "USER-004"
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "공유된 방을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                {
                    "status": 404,
                    "errorType": "Room Not Found",
                    "message": "공유된 방을 찾을 수 없습니다.",
                    "code": "ROOM-001"
                }
                """
                            )
                    )
            )
    })
    @PostMapping(value = "/collection/shared/{sharedRoomId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RoomOperationResponseDTO> saveSharedRoom(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @PathVariable("sharedRoomId") Long sharedRoomId,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("공유 방 저장 요청 - 커플 ID: {}, 공유방 ID: {}, 이미지 여부: {}",
                loginInfo.getCoupleId(),
                sharedRoomId,
                thumbnail != null && !thumbnail.isEmpty());

        RoomOperationResponseDTO response = collectionService.saveSharedRoom(
                loginInfo.getCoupleId(),
                sharedRoomId,
                thumbnail
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 저장된 방 상태 적용 API
     */
    @Operation(
            summary = "저장된 방 상태 적용",
            description = "컬렉션에서 선택한 방 상태를 현재 방에 적용합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "저장된 방 상태 적용 성공",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "로그인 필요",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @PostMapping("/collection/apply/{collectionRoomId}")
    public ResponseEntity<RoomOperationResponseDTO> applyRoomState(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @PathVariable("collectionRoomId") Long collectionRoomId) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("저장된 방 상태 적용 요청 - 커플 ID: {}, 컬렉션룸 ID: {}",
                loginInfo.getCoupleId(), collectionRoomId);
        collectionService.applyRoomState(loginInfo.getCoupleId(), collectionRoomId);

        return ResponseEntity.ok(RoomOperationResponseDTO.builder("저장된 방 상태가 성공적으로 적용되었습니다.")
                .addData("collectionRoomId", collectionRoomId)
                .build());
    }

    /**
     * 저장된 방 목록 조회 API
     */
    @Operation(
            summary = "저장된 방 목록 조회",
            description = """
        컬렉션에 저장된 모든 방 상태를 조회합니다.
        - 각 방의 ID, 소스(프리셋/공유/현재 방), 저장 날짜
        - 간략한 방 정보(벽지, 바닥, 썸네일 URL 등)
    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "저장된 방 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CollectionRoomResponseDTO.class),
                                    examples = @ExampleObject(value = """
                [
                    {
                        "id": 1,
                        "source": "PRESET",
                        "savedAt": "2024-03-19T15:30:00",
                        "roomPreview": {
                            "wallpaperName": "모던 벽지",
                            "floorName": "대리석 바닥",
                            "thumbnailUrl": "https://example.com/thumbnails/room1.jpg"
                        }
                    },
                    {
                        "id": 2,
                        "source": "CURRENT",
                        "savedAt": "2024-03-20T12:00:00",
                        "roomPreview": {
                            "wallpaperName": "북유럽 벽지",
                            "floorName": "원목 바닥",
                            "thumbnailUrl": "https://example.com/thumbnails/room2.jpg"
                        }
                    }
                ]
                """)
                            )
                    )
            }
    )
    @GetMapping("/collection")
    public ResponseEntity<List<CollectionRoomResponseDTO>> getSavedRooms(
            @AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("저장된 방 목록 조회 요청 - 커플 ID: {}", loginInfo.getCoupleId());
        List<CollectionRoomResponseDTO> rooms =
                collectionService.getSavedRooms(loginInfo.getCoupleId());

        return ResponseEntity.ok(rooms);
    }

    /**
     *  공유된 방 목록 조회
     * @param loginInfo
     * @return
     */
    @Operation(
            summary = "공유된 방 목록 조회",
            description = """
        다른 커플들이 공유한 방 목록을 조회합니다.
        - 각 방의 ID, 커플 이름, 방의 간단한 미리보기 정보
        - 공유된 시간과 썸네일 URL 포함
    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공유된 방 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SharedRoomResponseDTO.class),
                                    examples = @ExampleObject(value = """
                [
                    {
                        "roomId": 1,
                        "coupleName": "철수♥영희",
                        "roomPreview": {
                            "wallpaperName": "모던 벽지",
                            "floorName": "대리석 바닥",
                            "thumbnailUrl": "https://example.com/thumbnails/shared_room1.jpg"
                        },
                        "sharedAt": "2024-03-19T15:30:00"
                    },
                    {
                        "roomId": 2,
                        "coupleName": "민수♥수진",
                        "roomPreview": {
                            "wallpaperName": "북유럽 벽지",
                            "floorName": "원목 바닥",
                            "thumbnailUrl": "https://example.com/thumbnails/shared_room2.jpg"
                        },
                        "sharedAt": "2024-03-20T12:00:00"
                    }
                ]
                """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "로그인 필요",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "공유된 방을 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @GetMapping("/shared")
    public ResponseEntity<List<SharedRoomResponseDTO>> getSharedRooms(@AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("공유된 방 목록 조회 요청 - 커플 ID: {}", loginInfo.getCoupleId());
        List<SharedRoomResponseDTO> rooms =
                sharedRoomService.getSharedRooms(loginInfo.getCoupleId());

        return ResponseEntity.ok(rooms);
    }

    /**
     * 프리셋 방 목록 조회
     * @param loginInfo
     * @return
     */
    @Operation(
            summary = "프리셋 방 목록 조회",
            description = """
        서비스에서 제공하는 모든 프리셋 방 목록을 조회합니다.
        각 프리셋 방은 고유한 ID, 이름, 벽지 및 바닥 정보, 가구 배치 정보, 썸네일 URL을 포함합니다.
        """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "프리셋 방 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PresetRoomResponseDTO.class),
                                    examples = @ExampleObject(value = """
                    [
                        {
                            "presetId": 1,
                            "name": "클래식 룸",
                            "wallpaper": {
                                "id": 1,
                                "name": "모던 벽지",
                                "wallpaperNumber": 1
                            },
                            "floor": {
                                "id": 1,
                                "name": "원목 바닥",
                                "floorNumber": 1
                            },
                            "furnitureLayouts": [
                                {
                                    "furnitureId": 1,
                                    "name": "클래식 소파",
                                    "positionX": 100,
                                    "positionY": 200,
                                    "rotation": 90
                                }
                            ],
                            "thumbnailUrl": "https://example.com/thumbnail.jpg"
                        },
                        {
                            "presetId": 2,
                            "name": "모던 룸",
                            "wallpaper": {
                                "id": 2,
                                "name": "북유럽 벽지",
                                "wallpaperNumber": 2
                            },
                            "floor": {
                                "id": 2,
                                "name": "대리석 바닥",
                                "floorNumber": 2
                            },
                            "furnitureLayouts": [],
                            "thumbnailUrl": "https://example.com/thumbnail2.jpg"
                        }
                    ]
                """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "로그인이 필요합니다.",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @GetMapping("/presets")
    public ResponseEntity<List<PresetRoomResponseDTO>> getPresetRooms(
            @AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("프리셋 방 목록 조회 요청 - 사용자 ID: {}", loginInfo.getUserId());
        List<PresetRoomResponseDTO> presetRooms = presetRoomService.getAllPresetRooms();

        return ResponseEntity.ok(presetRooms);
    }

    /**
     * 방 공유 설정
     * @param loginInfo
     * @param isShared
     * @return
     */
    @Operation(
            summary = "방 공유 상태 설정",
            description = """
        현재 방의 공유 상태를 변경합니다.
        - 공유 상태가 `true`로 설정되면 다른 사용자들이 방을 볼 수 있습니다.
        - 공유 상태가 `false`로 설정되면 방은 비공개 상태가 됩니다.
    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공유 상태 변경 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RoomOperationResponseDTO.class),
                                    examples = @ExampleObject(value = """
                {
                    "message": "방 공유가 활성화되었습니다.",
                    "timestamp": "2024-03-19T15:30:00",
                    "data": {
                        "isShared": true
                    }
                }
                """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증되지 않은 사용자",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @PostMapping("/sharing")
    public ResponseEntity<RoomOperationResponseDTO> setRoomSharing(@AuthenticationPrincipal LoginInfo loginInfo, @RequestParam boolean isShared) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("방 공유 설정 요청 - 커플 ID: {}, 공유 상태: {}",
                loginInfo.getCoupleId(), isShared);
        RoomOperationResponseDTO response = sharedRoomService.setRoomSharing(loginInfo.getCoupleId(), isShared);

        return ResponseEntity.ok(response);
    }

}
