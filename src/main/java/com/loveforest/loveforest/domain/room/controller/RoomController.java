package com.loveforest.loveforest.domain.room.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.room.dto.PublicRoomResponseDTO;
import com.loveforest.loveforest.domain.room.dto.RoomDecorationRequestDTO;
import com.loveforest.loveforest.domain.room.dto.RoomFurnitureUpdateRequestDTO;
import com.loveforest.loveforest.domain.room.dto.RoomResponseDTO;
import com.loveforest.loveforest.domain.room.service.RoomService;
import com.loveforest.loveforest.domain.user.exception.LoginRequiredException;
import com.loveforest.loveforest.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final RoomService roomService;

    @Operation(
            summary = "방 가구 배치",
            description = "특정 커플의 방에 가구를 배치합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "가구 배치가 완료되었습니다.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청입니다. 유효하지 않은 커플 ID 또는 가구 ID입니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = "{\"status\": 400, \"errorType\": \"BadRequest\", \"message\": \"유효하지 않은 커플 ID입니다.\"}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "이미 해당 위치에 가구가 배치되어 있습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = "{\"status\": 409, \"errorType\": \"Conflict\", \"message\": \"해당 위치에 이미 가구가 배치되어 있습니다.\"}"
                                    )
                            )
                    )
            }
    )
    @PostMapping("/decorate")
    public ResponseEntity<String> decorateRoom(@AuthenticationPrincipal LoginInfo loginInfo, @RequestBody RoomDecorationRequestDTO request) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }
        roomService.decorateRoom(request);
        return ResponseEntity.ok("가구 배치가 완료되었습니다.");
    }

    @Operation(
            summary = "방 상태 조회",
            description = "특정 커플의 방에 배치된 가구 목록을 조회하는 API입니다. 이 API는 커플 ID를 이용해 해당 커플의 방에 배치된 가구들을 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "방 상태가 성공적으로 조회되었습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RoomResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "방을 찾을 수 없습니다. 유효하지 않은 커플 ID일 때 발생합니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = "{\"status\": 404, \"errorType\": \"NotFound\", \"message\": \"방을 찾을 수 없습니다.\"}"
                                    )
                            )
                    )
            }
    )
    @GetMapping("/my")
    public ResponseEntity<RoomResponseDTO> getRoom(@AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }
        RoomResponseDTO response = roomService.getRoomByCoupleId(loginInfo.getCoupleId());
        return ResponseEntity.ok(response);
    }

    /**
     * 방 가구 위치 이동 API
     */
    @Operation(
            summary = "가구 위치 이동",
            description = "특정 커플의 방에 배치된 가구의 위치와 회전 각도를 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "가구 위치 이동 성공",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "가구를 찾을 수 없습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 404, \"errorType\": \"NotFound\", \"message\": \"가구를 찾을 수 없습니다.\"}")
                            )
                    )
            }
    )
    @PutMapping("/furniture/{furnitureLayoutId}/move")
    public ResponseEntity<String> moveFurniture(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @PathVariable("furnitureLayoutId") Long furnitureLayoutId,
            @RequestBody RoomFurnitureUpdateRequestDTO request) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }
        roomService.moveFurniture(furnitureLayoutId, request);
        return ResponseEntity.ok("가구 위치가 성공적으로 수정되었습니다.");
    }

    /**
     * 방 가구 삭제 API
     */
    @Operation(
            summary = "가구 삭제",
            description = "방에 배치된 가구를 삭제합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "가구 삭제 성공",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "가구를 찾을 수 없습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"status\": 404, \"errorType\": \"NotFound\", \"message\": \"가구를 찾을 수 없습니다.\"}")
                            )
                    )
            }
    )
    @DeleteMapping("/furniture/{furnitureLayoutId}")
    public ResponseEntity<String> deleteFurniture(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @PathVariable("furnitureLayoutId") Long furnitureLayoutId) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }
        roomService.deleteFurniture(furnitureLayoutId);
        return ResponseEntity.ok("가구가 성공적으로 삭제되었습니다.");
    }

    @GetMapping("/public/{coupleId}")
    @Operation(
            summary = "다른 사용자의 커플방 조회",
            description = "다른 커플의 방 정보를 조회합니다. 공개된 정보만 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "방 조회 성공",
                            content = @Content(schema = @Schema(implementation = PublicRoomResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "방을 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    public ResponseEntity<PublicRoomResponseDTO> getPublicRoom(
            @Parameter(description = "조회할 커플의 ID", required = true)
            @PathVariable("coupleId") Long coupleId,
            @AuthenticationPrincipal LoginInfo loginInfo) {

        if (loginInfo == null) {
            throw new LoginRequiredException();
        }
        log.info("다른 커플의 방 조회 요청 - 커플 ID: {}", coupleId);
        PublicRoomResponseDTO response = roomService.getPublicRoomByCoupleId(coupleId);
        log.info("다른 커플의 방 조회 완료 - 커플 ID: {}", coupleId);
        return ResponseEntity.ok(response);
    }
}
