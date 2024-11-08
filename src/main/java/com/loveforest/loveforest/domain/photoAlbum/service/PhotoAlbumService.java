package com.loveforest.loveforest.domain.photoAlbum.service;

import com.loveforest.loveforest.domain.photoAlbum.dto.AIServerRequest;
import com.loveforest.loveforest.domain.photoAlbum.dto.PhotoAlbumRequestDTO;
import com.loveforest.loveforest.domain.photoAlbum.dto.PhotoAlbumResponseDTO;
import com.loveforest.loveforest.domain.photoAlbum.entity.PhotoAlbum;
import com.loveforest.loveforest.domain.photoAlbum.exception.AIServerPhotoException;
import com.loveforest.loveforest.domain.photoAlbum.exception.PhotoUploadFailedException;
import com.loveforest.loveforest.domain.photoAlbum.repository.PhotoAlbumRepository;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.exception.UserNotFoundException;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import com.loveforest.loveforest.exception.common.InvalidInputException;
import com.loveforest.loveforest.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PhotoAlbumService {
    private final PhotoAlbumRepository photoAlbumRepository;
    private final S3Service s3Service;
    private final WebClient.Builder webClientBuilder;
    private final UserRepository userRepository;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public PhotoAlbumResponseDTO savePhoto(PhotoAlbumRequestDTO request, Long userId) {
        try {
            // 1. 이미지 유효성 검사
            validateImage(request.getBase64Image());

            // 2. S3에 원본 이미지 업로드
            String imageUrl = uploadOriginalImage(request.getBase64Image());

            // 3. AI 서버에 3D 변환 요청
            byte[] modelData = convert3DModel(request);

            // 4. S3에 3D 모델 업로드
            String objectUrl = uploadModelFile(modelData);

            // 5. DB에 정보 저장
            return savePhotoAlbumData(imageUrl, objectUrl, request, userId);

        } catch (IllegalArgumentException e) {
            log.error("잘못된 입력 데이터: {}", e.getMessage());
            throw new PhotoUploadFailedException();
        } catch (Exception e) {
            log.error("사진 업로드 처리 중 오류: {}", e.getMessage());
            throw new PhotoUploadFailedException();
        }
    }

    private void validateImage(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new InvalidInputException();
        }
        // Base64 형식 검증
        if (!base64Image.matches("^data:image/.*;base64,.*")) {
            throw new InvalidInputException();
        }
    }

    private String uploadOriginalImage(String base64Image) {
        byte[] imageData = Base64.getDecoder().decode(
                base64Image.split(",")[1]  // "data:image/jpeg;base64," 부분 제거
        );
        return s3Service.uploadFile(imageData, ".jpg");
    }

    private byte[] convert3DModel(PhotoAlbumRequestDTO request) {
        WebClient webClient = webClientBuilder.baseUrl(aiServerUrl)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))  // 10MB로 설정
                .build();

        AIServerRequest aiRequest = new AIServerRequest(
                request.getBase64Image(),
                request.getPositionX(),
                request.getPositionY()
        );

        try {
            byte[] modelData = webClient.post()
                    .uri("/convert_3d_model")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(aiRequest)
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block(Duration.ofMinutes(2));  // 2분 타임아웃

            if (modelData == null || !isValidZipFile(modelData)) {
                throw new AIServerPhotoException();
            }

            return modelData;

        } catch (WebClientResponseException e) {
            log.error("AI 서버 오류: {} - {}", e.getStatusCode(), e.getMessage());
            throw new AIServerPhotoException();
        }
    }

    private boolean isValidZipFile(byte[] data) {
        // ZIP 파일 시그니처 확인
        return data.length >= 4 &&
                data[0] == 0x50 && data[1] == 0x4B &&
                data[2] == 0x03 && data[3] == 0x04;
    }

    private String uploadModelFile(byte[] modelData) {
        return s3Service.uploadFile(modelData, ".zip");
    }

    private PhotoAlbumResponseDTO savePhotoAlbumData(
            String imageUrl,
            String objectUrl,
            PhotoAlbumRequestDTO request,
            Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        PhotoAlbum photoAlbum = new PhotoAlbum(
                imageUrl,
                objectUrl,
                request.getPositionX(),
                request.getPositionY(),
                user
        );

        PhotoAlbum savedPhotoAlbum = photoAlbumRepository.save(photoAlbum);

        return new PhotoAlbumResponseDTO(
                savedPhotoAlbum.getId(),
                savedPhotoAlbum.getImageUrl(),
                savedPhotoAlbum.getObjectUrl(),
                savedPhotoAlbum.getPositionX(),
                savedPhotoAlbum.getPositionY()
        );
    }

    @Transactional(readOnly = true)
    public List<PhotoAlbumResponseDTO> getPhotos(Long userId) {
        return photoAlbumRepository.findByUserId(userId).stream()
                .map(photo -> new PhotoAlbumResponseDTO(
                        photo.getId(),
                        photo.getImageUrl(),
                        photo.getObjectUrl(),
                        photo.getPositionX(),
                        photo.getPositionY()
                ))
                .collect(Collectors.toList());
    }
}
