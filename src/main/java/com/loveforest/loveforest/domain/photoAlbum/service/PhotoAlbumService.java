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
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
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
     * 사진 등록 (3D 변환 없이 원본 이미지 저장)
     */
    public String savePhoto(PhotoAlbumRequestDTO request, Long userId) {
        validateImage(request.getBase64Image());

        // S3에 원본 이미지 업로드
        String imageUrl = uploadOriginalImage(request.getBase64Image());

        // DB에 저장 (3D 모델 관련 필드는 null 처리)
        savePhotoData(imageUrl, null, null, null, request, userId);

        return imageUrl;
    }

    /**
     * 3D 모델 변환
     */
    public List<String> convertPhotoTo3D(Long photoId, Long userId) {
        PhotoAlbum photoAlbum = photoAlbumRepository.findById(photoId)
                .orElseThrow(PhotoNotFoundException::new);

        // 권한 확인
        if (!photoAlbum.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }

        // AI 서버에 3D 변환 요청
        PhotoAlbumRequestDTO request = new PhotoAlbumRequestDTO(
                photoAlbum.getImageUrl(), photoAlbum.getPositionX(), photoAlbum.getPositionY()
        );
        List<byte[]> modelFiles = convert3DModel(request);

        // S3에 모델 업로드 및 URL 반환
        List<String> modelUrls = upload3DModelFiles(modelFiles);

        // DB 업데이트
        updatePhotoWithModelUrls(photoAlbum, modelUrls);

        return modelUrls;
    }

    private List<String> upload3DModelFiles(List<byte[]> modelFiles) {
        List<String> modelUrls = new ArrayList<>();
        modelUrls.add(s3Service.uploadFile(modelFiles.get(0), ".obj", "application/octet-stream", modelFiles.get(0).length));
        modelUrls.add(s3Service.uploadFile(modelFiles.get(1), ".png", "image/png", modelFiles.get(1).length));
        modelUrls.add(s3Service.uploadFile(modelFiles.get(2), ".mtl", "application/octet-stream", modelFiles.get(2).length));
        return modelUrls;
    }

    private void updatePhotoWithModelUrls(PhotoAlbum photoAlbum, List<String> modelUrls) {
        photoAlbum.updateModelUrls(
                modelUrls.get(0), // objUrl
                modelUrls.get(1), // pngUrl
                modelUrls.get(2)  // mtlUrl
        );
        photoAlbumRepository.save(photoAlbum);
    }


    private void savePhotoData(String imageUrl, String objUrl, String pngUrl, String mtlUrl,
                               PhotoAlbumRequestDTO request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        PhotoAlbum photoAlbum = new PhotoAlbum(
                imageUrl, objUrl, pngUrl, mtlUrl,
                request.getPositionX(), request.getPositionY(), user
        );

        photoAlbumRepository.save(photoAlbum);
    }





    /**
     * 실패 시 업로드된 파일들 정리
     */
    private void cleanupFailedUploads(List<String> uploadedUrls) {
        for (String url : uploadedUrls) {
            s3Service.deleteFile(url);
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
     * 이미지 유효성 검사
     */
    private void validateImage(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            log.error("이미지가 비어있음 또는 null");
            throw new InvalidInputException();
        }
        if (!base64Image.matches("^[A-Za-z0-9+/=]+$")) {
            log.error("이미지 형식이 유효하지 않음: {}", base64Image);
            throw new InvalidInputException();
        }
    }

    /**
     * 원본 이미지 업로드
     */
    private String uploadOriginalImage(String base64Image) {
        byte[] imageData = Base64.getDecoder().decode(base64Image); // Base64 디코딩
        String contentType = "image/jpeg"; // 이미지의 MIME 타입 설정
        long contentLength = imageData.length; // 파일 크기 계산

        return s3Service.uploadFile(
                imageData,           // 파일 데이터
                ".jpg",              // 파일 확장자
                contentType,         // MIME 타입
                contentLength        // 파일 크기
        );
    }
    /**
     * AI 서버에 3D 모델 변환 요청하여 .obj, .png, .mtl 파일을 List<byte[]>로 반환
     */
    private List<byte[]> convert3DModel(PhotoAlbumRequestDTO request) {
        WebClient webClient = webClientBuilder.baseUrl(aiServerUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB 설정
                .build();

        AIServerRequest aiRequest = new AIServerRequest(
                request.getBase64Image(),
                request.getPositionX(),
                request.getPositionY()
        );

        try {
            return webClient.post()
                    .uri("/convert_3d_model")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(aiRequest)
                    .accept(MediaType.MULTIPART_MIXED)
                    .retrieve()
                    .bodyToFlux(DataBuffer.class)
                    .map(this::toByteArray) // DataBuffer -> byte[] 변환
                    .collectList() // List<byte[]>로 수집
                    .block(Duration.ofSeconds(100)); // 10초 내 완료되지 않으면 타임아웃 발생

        } catch (WebClientResponseException e) {
            log.error("AI 서버 오류: {}", e.getMessage(), e);
            throw new AIServerPhotoException();
        } catch (Exception e) {
            log.error("사진 업로드 처리 중 오류: {}", e.getMessage(), e);
            throw new PhotoUploadFailedException();
        }
    }


    // DataBuffer를 byte[]로 변환하는 메서드
    private byte[] toByteArray(DataBuffer dataBuffer) {
        byte[] bytes = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(bytes);
        DataBufferUtils.release(dataBuffer);  // DataBuffer 메모리 해제
        return bytes;
    }


    /**
     * DB에 사진 정보 저장
     */
    private PhotoAlbumResponseDTO savePhotoAlbumData(
            String imageUrl,
            String objUrl,
            String pngUrl,
            String mtlUrl,
            PhotoAlbumRequestDTO request,
            Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        PhotoAlbum photoAlbum = new PhotoAlbum(
                imageUrl,
                objUrl,
                pngUrl,
                mtlUrl,
                request.getPositionX(),
                request.getPositionY(),
                user
        );

        PhotoAlbum savedPhotoAlbum = photoAlbumRepository.save(photoAlbum);

        return new PhotoAlbumResponseDTO(
                savedPhotoAlbum.getId(),
                savedPhotoAlbum.getImageUrl(),
                savedPhotoAlbum.getObjectUrl(),
                savedPhotoAlbum.getPngUrl(),
                savedPhotoAlbum.getMaterialUrl(),
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
                        photo.getPngUrl(),
                        photo.getMaterialUrl(),
                        photo.getPositionX(),
                        photo.getPositionY()
                ))
                .collect(Collectors.toList());
    }

}
