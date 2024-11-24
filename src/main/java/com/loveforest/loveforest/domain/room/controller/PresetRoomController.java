package com.loveforest.loveforest.domain.room.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.room.dto.PresetRoomResponseDTO;
import com.loveforest.loveforest.domain.room.entity.PresetRoom;
import com.loveforest.loveforest.domain.room.service.PresetRoomService;
import com.loveforest.loveforest.domain.user.exception.LoginRequiredException;
import com.loveforest.loveforest.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms/presets")
@RequiredArgsConstructor
public class PresetRoomController {
    private final PresetRoomService presetRoomService;

    @Operation(
            summary = "방 상태 프리셋 저장",
            description = "현재 방의 상태(벽지, 바닥, 가구 배치 등)를 프리셋으로 저장합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "프리셋 저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PresetRoomResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                        "presetId": 1,
                        "name": "My First Room",
                        "wallpaper": {
                            "id": 1,
                            "name": "클래식 벽지",
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
                        ]
                    }
                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "방을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                        "status": 404,
                        "errorType": "Room Not Found",
                        "message": "해당 방을 찾을 수 없습니다.",
                        "code": "ROOM-001"
                    }
                    """
                            )
                    )
            )
    })
    @PostMapping("/save/{roomId}")
    public ResponseEntity<PresetRoom> saveRoomAsPreset(
            @PathVariable("roomId") Long roomId,
            @RequestParam String presetName,
            @AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        PresetRoom preset = presetRoomService.saveRoomAsPreset(roomId, presetName);
        return ResponseEntity.ok(preset);
    }

    @Operation(
            summary = "프리셋 목록 조회",
            description = "저장된 모든 프리셋 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "프리셋 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PresetRoomResponseDTO.class))
                    )
            )
    })
    @PostMapping("/apply/{roomId}/{presetId}")
    public ResponseEntity<Void> applyPresetToRoom(
            @PathVariable("roomId") Long roomId,
            @PathVariable("presetId") Long presetId,
            @AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        presetRoomService.applyPresetToRoom(roomId, presetId);
        return ResponseEntity.ok().build();
    }
}
