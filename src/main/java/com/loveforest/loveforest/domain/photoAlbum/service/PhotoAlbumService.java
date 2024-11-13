package com.loveforest.loveforest.domain.photoAlbum.service;

import com.loveforest.loveforest.domain.photoAlbum.dto.AIServerRequest;
import com.loveforest.loveforest.domain.photoAlbum.dto.PhotoAlbumRequestDTO;
import com.loveforest.loveforest.domain.photoAlbum.dto.PhotoAlbumResponseDTO;
import com.loveforest.loveforest.domain.photoAlbum.entity.PhotoAlbum;
import com.loveforest.loveforest.domain.photoAlbum.exception.AIServerPhotoException;
import com.loveforest.loveforest.domain.photoAlbum.exception.PhotoNotFoundException;
import com.loveforest.loveforest.domain.photoAlbum.exception.PhotoUploadFailedException;
import com.loveforest.loveforest.domain.photoAlbum.repository.PhotoAlbumRepository;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.exception.UserNotFoundException;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import com.loveforest.loveforest.exception.common.InvalidInputException;
import com.loveforest.loveforest.exception.common.UnauthorizedException;
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

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * 사진 및 3D 모델 저장
     */
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

    /**
     * 사진 삭제
     */
    @Transactional
    public void deletePhoto(Long photoId, Long userId) {
        PhotoAlbum photo = photoAlbumRepository.findById(photoId)
                .orElseThrow(PhotoNotFoundException::new);

        // 권한 확인
        if (!photo.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }

        try {
            // S3에서 이미지와 3D 모델 파일 삭제
            s3Service.deleteFile(photo.getImageUrl());
            s3Service.deleteFile(photo.getObjectUrl());

            // DB에서 데이터 삭제
            photoAlbumRepository.delete(photo);

            log.info("사진 및 3D 모델 삭제 완료 - photoId: {}", photoId);
        } catch (Exception e) {
            log.error("사진 삭제 중 오류 발생 - photoId: {}", photoId, e);
            throw new PhotoNotFoundException();
        }
    }

    /**
     * 안전한 URL로 사진 조회
     * Presigned URL을 사용하여 보안 강화
     */
    @Transactional(readOnly = true)
    public PhotoAlbumResponseDTO getSecurePhoto(Long photoId, Long userId) {
        PhotoAlbum photo = photoAlbumRepository.findById(photoId)
                .orElseThrow(PhotoNotFoundException::new);

        // 권한 확인
        if (!photo.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }

        // Presigned URL 생성 (1시간 유효)
        String secureImageUrl = s3Service.generatePresignedUrl(
                extractKeyFromUrl(photo.getImageUrl()),
                60
        );
        String secureObjectUrl = s3Service.generatePresignedUrl(
                extractKeyFromUrl(photo.getObjectUrl()),
                60
        );

        return new PhotoAlbumResponseDTO(
                photo.getId(),
                secureImageUrl,
                secureObjectUrl,
                photo.getPositionX(),
                photo.getPositionY()
        );
    }

    /**
     * 전체 사진 목록 조회 (Presigned URL 사용)
     */
    @Transactional(readOnly = true)
    public List<PhotoAlbumResponseDTO> getSecurePhotos(Long userId) {
        return photoAlbumRepository.findByUserId(userId).stream()
                .map(photo -> {
                    String secureImageUrl = s3Service.generatePresignedUrl(
                            extractKeyFromUrl(photo.getImageUrl()),
                            60
                    );
                    String secureObjectUrl = s3Service.generatePresignedUrl(
                            extractKeyFromUrl(photo.getObjectUrl()),
                            60
                    );

                    return new PhotoAlbumResponseDTO(
                            photo.getId(),
                            secureImageUrl,
                            secureObjectUrl,
                            photo.getPositionX(),
                            photo.getPositionY()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 실패 시 업로드된 파일들 정리
     */
    private void cleanupFailedUpload(String imageUrl, String objectUrl) {
        try {
            if (imageUrl != null) {
                s3Service.deleteFile(imageUrl);
            }
            if (objectUrl != null) {
                s3Service.deleteFile(objectUrl);
            }
        } catch (Exception e) {
            log.error("실패한 업로드 정리 중 오류 발생", e);
        }
    }

    private String extractKeyFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }



    /**
     * 이미지 유효성 검사
     */
    private void validateImage(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new InvalidInputException();
        }
        // Base64 형식 검증
        if (!base64Image.matches("^data:image/.*;base64,.*")) {
            throw new InvalidInputException();
        }
    }

    /**
     * 원본 이미지 업로드
     */
    private String uploadOriginalImage(String base64Image) {
        byte[] imageData = Base64.getDecoder().decode(
                base64Image.split(",")[1]  // "data:image/jpeg;base64," 부분 제거
        );
        return s3Service.uploadFile(imageData, ".jpg");
    }

    /**
     * 3D 모델 파일 업로드
     */
    private String uploadModelFile(byte[] modelData) {
        return s3Service.uploadFile(modelData, ".zip");
    }

    /**
     * AI 서버에 3D 모델 변환 요청
     */
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


    /**
     * DB에 사진 정보 저장
     */
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
