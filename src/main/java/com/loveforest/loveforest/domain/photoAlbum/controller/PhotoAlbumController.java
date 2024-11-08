package com.loveforest.loveforest.domain.photoAlbum.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.photoAlbum.dto.PhotoAlbumRequestDTO;
import com.loveforest.loveforest.domain.photoAlbum.dto.PhotoAlbumResponseDTO;
import com.loveforest.loveforest.domain.photoAlbum.service.PhotoAlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/photo-album")
@RequiredArgsConstructor
@Tag(name = "사진첩 API", description = "사진첩 관련 API")
public class PhotoAlbumController {

    private final PhotoAlbumService photoAlbumService;

    @Operation(summary = "사진 등록", description = "새로운 사진을 등록하고 AI 서버를 통해 3D 오브젝트로 변환합니다.")
    @ApiResponse(responseCode = "200", description = "사진 등록 및 3D 변환 성공")
    @PostMapping
    public ResponseEntity<PhotoAlbumResponseDTO> savePhoto(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @Valid @RequestBody PhotoAlbumRequestDTO request) {

        log.info("사진 등록 요청 - 사용자 ID: {}, 좌표: ({}, {})",
                loginInfo.getUserId(), request.getPositionX(), request.getPositionY());

        PhotoAlbumResponseDTO response = photoAlbumService.savePhoto(request, loginInfo.getUserId());

        log.info("사진 등록 완료 - 사진 ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사진 목록 조회", description = "사용자의 모든 사진과 3D 오브젝트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "사진 목록 조회 성공")
    @GetMapping
    public ResponseEntity<List<PhotoAlbumResponseDTO>> getPhotos(
            @AuthenticationPrincipal LoginInfo loginInfo) {

        log.info("사진 목록 조회 요청 - 사용자 ID: {}", loginInfo.getUserId());
        List<PhotoAlbumResponseDTO> photos = photoAlbumService.getPhotos(loginInfo.getUserId());
        log.info("사진 목록 조회 완료 - 조회된 사진 수: {}", photos.size());

        return ResponseEntity.ok(photos);
    }
}
