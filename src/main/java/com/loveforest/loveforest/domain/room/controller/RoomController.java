package com.loveforest.loveforest.domain.room.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.room.dto.*;
import com.loveforest.loveforest.domain.room.service.RoomServiceImpl;
import com.loveforest.loveforest.domain.user.exception.LoginRequiredException;
import com.loveforest.loveforest.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "방 꾸미기 API", description = "방 꾸미기 관련 API 입니다.")
public class RoomController {

    private final RoomServiceImpl roomServiceImpl;


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
}
