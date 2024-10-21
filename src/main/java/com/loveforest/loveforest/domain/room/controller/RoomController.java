package com.loveforest.loveforest.domain.room.controller;

import com.loveforest.loveforest.domain.room.dto.RoomDecorationRequestDTO;
import com.loveforest.loveforest.domain.room.dto.RoomResponseDTO;
import com.loveforest.loveforest.domain.room.service.RoomService;
import com.loveforest.loveforest.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> decorateRoom(@RequestBody RoomDecorationRequestDTO request) {
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
    @GetMapping("/{coupleId}")
    public ResponseEntity<RoomResponseDTO> getRoom(@PathVariable Long coupleId) {
        RoomResponseDTO response = roomService.getRoomByCoupleId(coupleId);
        return ResponseEntity.ok(response);
    }
}
