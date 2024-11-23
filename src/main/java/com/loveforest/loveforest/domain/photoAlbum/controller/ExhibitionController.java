package com.loveforest.loveforest.domain.photoAlbum.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.photoAlbum.dto.ExhibitionRequestDTO;
import com.loveforest.loveforest.domain.photoAlbum.dto.ExhibitionResponseDTO;
import com.loveforest.loveforest.domain.photoAlbum.exception.ExhibitionAlreadyExistsException;
import com.loveforest.loveforest.domain.photoAlbum.service.ExhibitionService;
import com.loveforest.loveforest.domain.user.exception.LoginRequiredException;
import com.loveforest.loveforest.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exhibition")
@RequiredArgsConstructor
@Tag(name = "전시 API", description = "3D 오브젝트 전시 관련 API")
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    /**
     * 전시 정보 조회
     * @param exhibitionId
     * @param loginInfo
     * @return
     */
    @Operation(summary = "전시 정보 조회", description = "전시된 3D 오브젝트와 사진의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "전시 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExhibitionResponseDTO.class),
                            examples = @ExampleObject(value = """
                    {
                        "exhibitionId": 1,
                        "objectUrl": "https://s3.amazonaws.com/bucket/object.obj",
                        "textureUrl": "https://s3.amazonaws.com/bucket/texture.png",
                        "materialUrl": "https://s3.amazonaws.com/bucket/material.mtl",
                        "positionX": 10.5,
                        "positionY": 20.3,
                        "exhibitedAt": "2024-01-20T14:30:00",
                        "photo": {
                            "photoId": 1,
                            "title": "우리의 첫 데이트",
                            "imageUrl": "https://s3.amazonaws.com/bucket/image.jpg",
                            "photoDate": "2024-01-20",
                            "description": "행복했던 그 날의 기록"
                        }
                    }
                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "전시 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{exhibitionId}")
    public ResponseEntity<ExhibitionResponseDTO> getExhibition(
            @PathVariable("exhibitionId") Long exhibitionId,
            @AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        ExhibitionResponseDTO response = exhibitionService.getExhibition(exhibitionId, loginInfo.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * 전시물 등록
     * @param request
     * @param loginInfo
     * @return
     */
    @Operation(summary = "전시물 등록", description = "새로운 3D 오브젝트를 지정된 위치에 전시합니다. 하나의 사진은 한 번만 전시될 수 있습니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "전시물 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExhibitionResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 전시된 사진",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 위치 값",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<ExhibitionResponseDTO> createExhibition(
            @Valid @RequestBody ExhibitionRequestDTO request,
            @AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        // 이미 전시된 물체가 있는지 확인
        if (exhibitionService.isExhibitionExists(request.getPhotoId())) {
            throw new ExhibitionAlreadyExistsException();
        }

        ExhibitionResponseDTO response = exhibitionService.createExhibition(request, loginInfo.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 전시물 삭제
     * @param exhibitionId
     * @param loginInfo
     * @return
     */
    @Operation(summary = "전시물 삭제", description = "전시된 3D 오브젝트를 삭제합니다. 삭제 후 해당 사진은 다시 전시될 수 있습니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "전시물 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "전시물을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{exhibitionId}")
    public ResponseEntity<Void> deleteExhibition(
            @PathVariable("exhibitionId") Long exhibitionId,
            @AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        exhibitionService.deleteExhibition(exhibitionId, loginInfo.getUserId());
        return ResponseEntity.noContent().build();
    }


    /**
     * 전시 중인 사진 목록 조회
     * @param loginInfo
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "전시 중인 사진 목록 조회",
            description = """
        현재 전시장에 전시되어 있는 모든 3D 오브젝트와 사진들의 정보를 조회합니다.
        - 전시물의 3D 모델 파일 URL (obj, texture, material)
        - 전시물의 위치 정보 (x, y 좌표)
        - 원본 사진 정보 및 메타데이터
        - 전시 시작 시간 정보를 포함합니다.
        """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "전시 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ExhibitionResponseDTO.class)),
                            examples = @ExampleObject(value = """
            [
                {
                    "exhibitionId": 1,
                    "objectUrl": "https://s3.amazonaws.com/bucket/object_1.obj",
                    "textureUrl": "https://s3.amazonaws.com/bucket/texture_1.png",
                    "materialUrl": "https://s3.amazonaws.com/bucket/material_1.mtl",
                    "positionX": 10.5,
                    "positionY": 20.3,
                    "exhibitedAt": "2024-01-20T14:30:00",
                    "photo": {
                        "photoId": 1,
                        "title": "우리의 첫 데이트",
                        "imageUrl": "https://s3.amazonaws.com/bucket/image_1.jpg",
                        "photoDate": "2024-01-20",
                        "description": "행복했던 그 날의 기록"
                    }
                },
                {
                    "exhibitionId": 2,
                    "objectUrl": "https://s3.amazonaws.com/bucket/object_2.obj",
                    "textureUrl": "https://s3.amazonaws.com/bucket/texture_2.png",
                    "materialUrl": "https://s3.amazonaws.com/bucket/material_2.mtl",
                    "positionX": 30.0,
                    "positionY": 40.0,
                    "exhibitedAt": "2024-01-21T15:45:00",
                    "photo": {
                        "photoId": 2,
                        "title": "즐거운 피크닉",
                        "imageUrl": "https://s3.amazonaws.com/bucket/image_2.jpg",
                        "photoDate": "2024-01-21",
                        "description": "공원에서의 특별한 하루"
                    }
                }
            ]
            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
            {
                "status": 401,
                "errorType": "Unauthorized",
                "message": "로그인이 필요합니다.",
                "code": "USER-004",
                "timestamp": "2024-01-22T10:00:00"
            }
            """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "전시물이 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
            {
                "status": 404,
                "errorType": "NotFound",
                "message": "전시된 사진이 없습니다.",
                "code": "EXHIBITION-001",
                "timestamp": "2024-01-22T10:00:00"
            }
            """)
                    )
            )
    })
    public ResponseEntity<List<ExhibitionResponseDTO>> getExhibitionList(@AuthenticationPrincipal LoginInfo loginInfo) {

        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        List<ExhibitionResponseDTO> exhibitions = exhibitionService.getExhibitionList(loginInfo.getUserId());
        return ResponseEntity.ok(exhibitions);
    }
}