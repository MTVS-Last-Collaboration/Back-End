package com.loveforest.loveforest.domain.photoAlbum.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.photoAlbum.dto.ApiResponseDTO;
import com.loveforest.loveforest.domain.photoAlbum.dto.PhotoAlbumRequestDTO;
import com.loveforest.loveforest.domain.photoAlbum.dto.PhotoAlbumResponseDTO;
import com.loveforest.loveforest.domain.photoAlbum.service.PhotoAlbumService;
import com.loveforest.loveforest.domain.user.exception.LoginRequiredException;
import io.swagger.v3.oas.annotations.Operation;
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

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/photo-album")
@RequiredArgsConstructor
@Tag(name = "사진첩 API", description = "사진첩 관련 API")
public class PhotoAlbumController {

    private final PhotoAlbumService photoAlbumService;
    /**
     * 사진 등록 (3D 변환 없이 원본 이미지 저장)
     */
    @Operation(summary = "사진 등록", description = "새로운 사진을 등록합니다.")
    @ApiResponse(responseCode = "200", description = "사진 등록 성공")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDTO<PhotoAlbumResponseDTO>> savePhoto(@AuthenticationPrincipal LoginInfo loginInfo,
                                                            @RequestPart("title") String title,
                                                            @RequestPart("content") String content,
                                                            @RequestPart("photoDate") String photoDate,
                                                            @RequestPart("photo") MultipartFile photo) {

        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        // `photoDate`를 LocalDate로 변환
        LocalDate parsedDate = LocalDate.parse(photoDate);

        // DTO 생성
        PhotoAlbumRequestDTO request = new PhotoAlbumRequestDTO(title, content, parsedDate, photo);

        PhotoAlbumResponseDTO responseDTO = photoAlbumService.savePhoto(request, loginInfo.getUserId());

        log.info("사진 등록 완료 - 제목: {}, 이미지 URL: {}", request.getTitle(), responseDTO.getImageUrl());
        return ResponseEntity.ok(ApiResponseDTO.success("사진이 성공적으로 등록되었습니다.", responseDTO));
    }

    /**
     * 3D 모델 변환
     */
    @Operation(
            summary = "3D 모델 변환",
            description = "저장된 사진을 기반으로 3D 오브젝트를 생성합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "3D 변환 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ApiResponseDTO.class),
                                    examples = @ExampleObject(
                                            value = """
                        {
                          "message": "3D 모델 변환이 완료되었습니다.",
                          "data": [
                            "https://loveforest.s3.ap-northeast-2.amazonaws.com/30b8fee9-9f5c-4578-8d82-cfb83f524e2a.obj",
                            "https://loveforest.s3.ap-northeast-2.amazonaws.com/12ca0b9e-f19c-4dd8-8cc3-7611af098cb5.png",
                            "https://loveforest.s3.ap-northeast-2.amazonaws.com/416d9c9f-7a66-458f-88be-0668cdd1da1f.mtl"
                          ]
                        }
                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/convert/{photoId}")
    public ResponseEntity<ApiResponseDTO<List<String>>> convertTo3DModel(@AuthenticationPrincipal LoginInfo loginInfo, @PathVariable("photoId") Long photoId,
            @RequestParam Double positionX, @RequestParam Double positionY) {

        List<String> modelUrls = photoAlbumService.convert3DModel(
                photoId,
                loginInfo.getUserId(),
                positionX,
                positionY
        );
        return ResponseEntity.ok(ApiResponseDTO.success("3D 모델 변환이 완료되었습니다.", modelUrls));
    }

    /**
     * 사진 삭제
     * @param loginInfo
     * @param photoId
     * @return
     */
    @Operation(summary = "사진 삭제", description = "지정된 사진과 관련 3D 모델을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "사진 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "사진을 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음")
    })
    @DeleteMapping("/{photoId}")
    public ResponseEntity<ApiResponseDTO<Void>> deletePhoto(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @PathVariable Long photoId) {

        if (loginInfo == null) {
            throw new LoginRequiredException();
        }


        log.info("사진 삭제 요청 - 사용자 ID: {}, 사진 ID: {}",
                loginInfo.getUserId(), photoId);

        photoAlbumService.deletePhoto(photoId, loginInfo.getUserId());

        log.info("사진 삭제 완료 - 사진 ID: {}", photoId);
        return ResponseEntity.ok(ApiResponseDTO.success("사진이 성공적으로 삭제되었습니다.", null));
    }


    /**
     * 사진 목록 조회
     * @param loginInfo
     * @return
     */
    @Operation(summary = "사진 목록 조회", description = "사용자의 모든 사진과 3D 오브젝트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "사진 목록 조회 성공")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<PhotoAlbumResponseDTO>>> getPhotos(
            @AuthenticationPrincipal LoginInfo loginInfo) {

        if (loginInfo == null) {
            throw new LoginRequiredException();
        }


        log.info("사진 목록 조회 요청 - 사용자 ID: {}", loginInfo.getUserId());
        List<PhotoAlbumResponseDTO> photos = photoAlbumService.getPhotos(loginInfo.getUserId());
        log.info("사진 목록 조회 완료 - 조회된 사진 수: {}", photos.size());

        return ResponseEntity.ok(ApiResponseDTO.success(photos));
    }

    /**
     * 사진 일괄 삭제
     * @param loginInfo
     * @param photoIds
     * @return
     */
    @Operation(
            summary = "사진 일괄 삭제",
            description = "여러 장의 사진을 한 번에 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "사진 일괄 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @DeleteMapping("/bulk")
    public ResponseEntity<ApiResponseDTO<Void>> bulkDeletePhotos(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @RequestBody List<Long> photoIds) {

        if (loginInfo == null) {
            throw new LoginRequiredException();
        }


        log.info("사진 일괄 삭제 요청 - 사용자 ID: {}, 사진 수: {}",
                loginInfo.getUserId(), photoIds.size());

        photoIds.forEach(photoId ->
                photoAlbumService.deletePhoto(photoId, loginInfo.getUserId()));

        log.info("사진 일괄 삭제 완료 - 삭제된 사진 수: {}", photoIds.size());
        return ResponseEntity.ok(ApiResponseDTO.success("사진이 일괄 삭제되었습니다.", null));
    }
}
