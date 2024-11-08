package com.loveforest.loveforest.domain.photoAlbum.service;

import com.loveforest.loveforest.domain.photoAlbum.dto.AIServerRequest;
import com.loveforest.loveforest.domain.photoAlbum.dto.PhotoAlbumRequestDTO;
import com.loveforest.loveforest.domain.photoAlbum.dto.PhotoAlbumResponseDTO;
import com.loveforest.loveforest.domain.photoAlbum.entity.PhotoAlbum;
import com.loveforest.loveforest.domain.photoAlbum.exception.AIServerPhotoException;
import com.loveforest.loveforest.domain.photoAlbum.exception.PhotoUploadFailedException;
import com.loveforest.loveforest.domain.photoAlbum.repository.PhotoAlbumRepository;
import com.loveforest.loveforest.domain.user.exception.UserNotFoundException;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import com.loveforest.loveforest.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

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
            // 1. S3에 이미지 업로드 (base64 디코딩 후 업로드)
            byte[] imageData = Base64.getDecoder().decode(request.getBase64Image());
            String imageUrl = s3Service.uploadFile(imageData, ".jpg");

            // 2. AI 서버에 base64 이미지 그대로 전송 및 3D 오브젝트 수신
            WebClient webClient = webClientBuilder.baseUrl(aiServerUrl).build();

            AIServerRequest aiRequest = new AIServerRequest(
                    request.getBase64Image(),
                    request.getPositionX(),
                    request.getPositionY()
            );

            byte[] objectData;
            try {
                objectData = webClient.post()
                        .uri("/convert_3d_model")
                        .bodyValue(aiRequest)  // base64 이미지와 좌표 정보 함께 전송
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .block();
                if (objectData == null) {
                    throw new AIServerPhotoException();
                }
            } catch (Exception e) {
                log.error("AI 서버 처리 실패: {}", e.getMessage());
                throw new AIServerPhotoException();
            }

            // 3. 3D 오브젝트를 S3에 업로드
            String objectUrl = s3Service.uploadFile(objectData, ".zip");

            // 4. DB에 정보 저장
            PhotoAlbum photoAlbum = new PhotoAlbum(
                    imageUrl,
                    objectUrl,
                    request.getPositionX(),
                    request.getPositionY(),
                    userRepository.findById(userId)
                            .orElseThrow(UserNotFoundException::new)
            );

            PhotoAlbum savedPhotoAlbum = photoAlbumRepository.save(photoAlbum);
            return new PhotoAlbumResponseDTO(
                    savedPhotoAlbum.getId(),
                    savedPhotoAlbum.getImageUrl(),
                    savedPhotoAlbum.getObjectUrl(),
                    savedPhotoAlbum.getPositionX(),
                    savedPhotoAlbum.getPositionY()
            );
        } catch (IllegalArgumentException e) {
            log.error("잘못된 Base64 이미지 형식: {}", e.getMessage());
            throw new PhotoUploadFailedException();
        } catch (Exception e) {
            log.error("사진 업로드 중 오류 발생: {}", e.getMessage());
            throw new PhotoUploadFailedException();
        }
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
