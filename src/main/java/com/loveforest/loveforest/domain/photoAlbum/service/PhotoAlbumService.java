package com.loveforest.loveforest.domain.photoAlbum.service;

import com.loveforest.loveforest.domain.photoAlbum.dto.AIServerRequest;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;
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
    public String savePhoto(MultipartFile photo, Long userId) {

        validateImage(photo);

        String imageUrl = uploadOriginalImage(photo);

        savePhotoData(imageUrl, null, null, null, userId, null, null);
        return imageUrl;
    }

    /**
     * 이미지 유효성 검사
     */
    private void validateImage(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new InvalidInputException();
        }

        String contentType = photo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidInputException();
        }
    }

    private void savePhotoData(String imageUrl, String objUrl, String pngUrl, String mtlUrl,
                               Long userId, Double positionX, Double positionY) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        PhotoAlbum photoAlbum = new PhotoAlbum(
                imageUrl, objUrl, pngUrl, mtlUrl,
                positionX, positionY, user
        );

        photoAlbumRepository.save(photoAlbum);
    }

    /**
     * 3D 모델 변환
     */
    public List<String> convert3DModel(Long photoId, Long userId, Double positionX, Double positionY) {
        PhotoAlbum photoAlbum = photoAlbumRepository.findById(photoId)
                .orElseThrow(PhotoNotFoundException::new);

        if (!photoAlbum.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }

        try {
            List<byte[]> modelFiles = requestAIServerConversion(photoAlbum.getImageUrl(), positionX, positionY);
            List<String> modelUrls = upload3DModelFiles(modelFiles);

            photoAlbum.updateModelUrlsAndPosition(
                    modelUrls.get(0), // obj
                    modelUrls.get(1), // png
                    modelUrls.get(2), // mtl
                    positionX,
                    positionY
            );

            photoAlbumRepository.save(photoAlbum);
            return modelUrls;

        } catch (Exception e) {
            log.error("3D 변환 실패: {}", e.getMessage());
            throw new PhotoUploadFailedException();
        }
    }

    private List<byte[]> requestAIServerConversion(String imageUrl, Double positionX, Double positionY) {
        WebClient webClient = webClientBuilder.baseUrl(aiServerUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(20 * 1024 * 1024)) // 20MB로 증가
                .build();

        AIServerRequest request = new AIServerRequest(imageUrl, positionX, positionY);

        return webClient.post()
                .uri("/convert_3d_model")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .map(this::toByteArray)
                .collectList()
                .timeout(Duration.ofMinutes(2))
                .block();
    }

    private List<String> upload3DModelFiles(List<byte[]> modelFiles) {
        List<String> modelUrls = new ArrayList<>();
        modelUrls.add(s3Service.uploadFile(modelFiles.get(0), ".obj", "application/octet-stream", modelFiles.get(0).length));
        modelUrls.add(s3Service.uploadFile(modelFiles.get(1), ".png", "image/png", modelFiles.get(1).length));
        modelUrls.add(s3Service.uploadFile(modelFiles.get(2), ".mtl", "application/octet-stream", modelFiles.get(2).length));
        return modelUrls;
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
     * 원본 이미지 업로드
     */
    private String uploadOriginalImage(MultipartFile photo) {
        try {
            String extension = getExtension(photo.getOriginalFilename());
            return s3Service.uploadFile(
                    photo.getBytes(),
                    extension,
                    photo.getContentType(),
                    photo.getSize()
            );
        } catch (IOException e) {
            throw new PhotoUploadFailedException();
        }
    }

    private String getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".")))
                .orElse(".jpg");
    }


    // DataBuffer를 byte[]로 변환하는 메서드
    private byte[] toByteArray(DataBuffer dataBuffer) {
        byte[] bytes = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(bytes);
        DataBufferUtils.release(dataBuffer);  // DataBuffer 메모리 해제
        return bytes;
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
