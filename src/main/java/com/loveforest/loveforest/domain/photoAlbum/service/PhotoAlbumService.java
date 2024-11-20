package com.loveforest.loveforest.domain.photoAlbum.service;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.exception.CoupleNotFoundException;
import com.loveforest.loveforest.domain.photoAlbum.dto.AIServerRequest;
import com.loveforest.loveforest.domain.photoAlbum.dto.PhotoAlbumRequestDTO;
import com.loveforest.loveforest.domain.photoAlbum.dto.PhotoAlbumResponseDTO;
import com.loveforest.loveforest.domain.photoAlbum.entity.PhotoAlbum;
import com.loveforest.loveforest.domain.photoAlbum.exception.Photo3DConvertFailedException;
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
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import java.util.concurrent.atomic.AtomicInteger;
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

    private final AtomicInteger counter = new AtomicInteger(0);




    /**
     * 사진 등록 (메타데이터 포함)
     *
     * @param request 사진 정보를 담은 DTO (제목, 내용, 사진 날짜, 파일 등)
     * @param userId  현재 사용자 ID
     * @return 저장된 사진 정보가 포함된 PhotoAlbumResponseDTO
     */
    public PhotoAlbumResponseDTO savePhoto(PhotoAlbumRequestDTO request, Long userId) {
        validateImage(request.getPhoto());
        validatePhotoDate(request.getPhotoDate());

        String imageUrl = uploadOriginalImage(request.getPhoto());

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 사용자의 커플 정보 가져오기
        Couple couple = user.getCouple();
        if (couple == null) {
            throw new CoupleNotFoundException();
        }

        PhotoAlbum photoAlbum = new PhotoAlbum(
                request.getTitle(),
                request.getContent(),
                request.getPhotoDate(),
                imageUrl,
                null, // objectUrl
                null, // pngUrl
                null, // materialUrl
                null, // positionX
                null, // positionY
                user,
                couple
        );

        photoAlbumRepository.save(photoAlbum);
        log.info("사진 저장 완료 - 제목: {}, 작성자: {}, 커플ID: {}",
                request.getTitle(), user.getNickname(), couple.getId());

        return new PhotoAlbumResponseDTO(photoAlbum.getId(), photoAlbum.getTitle(), photoAlbum.getContent(), photoAlbum.getPhotoDate(),
                imageUrl, null, null, null, null, null);
    }

    /**
     * 사진 날짜 유효성 검증
     *
     * @param photoDate 업로드 요청에 포함된 사진 날짜
     */
    private void validatePhotoDate(LocalDate photoDate) {
        if (photoDate == null || photoDate.isAfter(LocalDate.now())) {
            throw new InvalidInputException();
        }
    }

    /**
     * 이미지 유효성 검사
     *
     * @param photo 업로드 요청에 포함된 MultipartFile 사진
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

    /**
     * 3D 모델 변환
     *
     * @param photoId    변환할 사진 ID
     * @param userId     현재 사용자 ID
     * @param positionX  X 좌표
     * @param positionY  Y 좌표
     * @return 변환된 3D 모델의 URL 리스트
     */
    public List<String> convert3DModel(Long photoId, Long userId, Integer positionX, Integer positionY) {
        PhotoAlbum photoAlbum = photoAlbumRepository.findById(photoId)
                .orElseThrow(PhotoNotFoundException::new);

        if (!photoAlbum.getUser().getId().equals(userId)) {
            throw new UnauthorizedException();
        }

        try {
            List<String> modelFiles = requestAIServerConversion(photoAlbum.getImageUrl(), positionX, positionY);
            List<String> modelUrls = saveModelFiles(modelFiles);

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
            throw new Photo3DConvertFailedException();
        }
    }

    /**
     * AI 서버에 변환 요청
     *
     * @param imageUrl  변환할 원본 이미지 URL
     * @param positionX X 좌표
     * @param positionY Y 좌표
     * @return 변환된 3D 모델의 파일 데이터 리스트
     */
    public List<String> requestAIServerConversion(String imageUrl, int positionX, int positionY) {
        WebClient webClient = webClientBuilder.baseUrl(aiServerUrl).build();

        String base64Image = downloadAndEncodeImage(imageUrl);
        AIServerRequest request = new AIServerRequest(base64Image, positionX, positionY);

        try {
            log.info("AI 서버에 요청: {}", request);

            WebClient.ResponseSpec responseSpec = webClient.post()
                    .uri("/convert_3d_model")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            status -> !status.is2xxSuccessful(),
                            response -> response.bodyToMono(String.class)
                                    .doOnNext(body -> log.error("AI 서버 응답 오류: {}", body))
                                    .then(Mono.error(new Photo3DConvertFailedException()))
                    );

            log.info("AI 서버 응답 데이터: {}", responseSpec.bodyToMono(String.class).block());
            return processMultipartResponse(responseSpec);

        } catch (Exception e) {
            log.error("AI 서버 변환 실패: {}", e.getMessage());
            throw new Photo3DConvertFailedException();
        }
    }



    private Map<String, byte[]> parseMultipartMixedResponse(WebClient.ResponseSpec responseSpec) {
        String boundary = "myboundary"; // Python API에서 사용하는 boundary와 동일하게 설정
        return responseSpec.bodyToFlux(DataBuffer.class)
                .map(dataBuffer -> {
                    try {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        return bytes;
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                })
                .collectList()
                .map(dataBuffers -> splitMultipartMixed(dataBuffers, boundary))
                .block();
    }

    private Map<String, byte[]> splitMultipartMixed(List<byte[]> dataBuffers, String boundary) {
        Map<String, byte[]> fileParts = new HashMap<>();
        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (byte[] part : dataBuffers) {
            buffer.writeBytes(part);
        }

        String content = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        String[] parts = content.split(new String(boundaryBytes));

        for (String part : parts) {
            if (part.contains("Content-Disposition")) {
                String[] headersAndBody = part.split("\r\n\r\n", 2);
                if (headersAndBody.length > 1) {
                    String headers = headersAndBody[0];
                    String body = headersAndBody[1];

                    if (headers.contains("output_file.obj")) {
                        fileParts.put("output_file.obj", body.getBytes(StandardCharsets.UTF_8));
                    } else if (headers.contains("output_file.png")) {
                        fileParts.put("output_file.png", body.getBytes(StandardCharsets.UTF_8));
                    } else if (headers.contains("output_file.mtl")) {
                        fileParts.put("output_file.mtl", body.getBytes(StandardCharsets.UTF_8));
                    }
                }
            }
        }

        return fileParts;
    }


    private List<String> processMultipartResponse(WebClient.ResponseSpec responseSpec) {
        // multipart 응답을 파싱
        Map<String, byte[]> parts = parseMultipartMixedResponse(responseSpec);

        if (!parts.containsKey("output_file.obj") ||
                !parts.containsKey("output_file.png") ||
                !parts.containsKey("output_file.mtl")) {
            throw new Photo3DConvertFailedException();
        }

        // 각 파일을 S3에 업로드
        List<String> urls = new ArrayList<>();
        urls.add(s3Service.uploadFile(
                parts.get("output_file.obj"),
                ".obj",
                "application/x-tgif",
                parts.get("output_file.obj").length
        ));
        // png, mtl 파일도 동일하게 처리

        return urls;
    }


    /**
     * 3D 모델 파일 저장
     */
    private List<String> saveModelFiles(List<String> modelData) {
        List<String> urls = new ArrayList<>();
        try {
            for (int i = 0; i < modelData.size(); i++) {
                String extension = switch (i) {
                    case 0 -> ".obj";
                    case 1 -> ".png";
                    case 2 -> ".mtl";
                    default -> throw new IllegalStateException("Unexpected value: " + i);
                };
                String contentType = switch (i) {
                    case 0, 2 -> "application/octet-stream";
                    case 1 -> "image/png";
                    default -> "application/octet-stream";
                };

                // String을 byte[]로 변환
                byte[] fileBytes = Base64.getDecoder().decode(modelData.get(i));

                urls.add(s3Service.uploadFile(
                        fileBytes,  // 바이트 배열로 변환된 데이터
                        extension,
                        contentType,
                        fileBytes.length  // 실제 파일의 바이트 크기
                ));
            }
            return urls;
        } catch (Exception e) {
            log.error("모델 파일 저장 중 에러 발생: {}", e.getMessage());
            throw new PhotoUploadFailedException();
        }
    }

    /**
     * S3에서 이미지 다운로드 및 Base64 인코딩
     */
    private String downloadAndEncodeImage(String imageUrl) {
        try {
            byte[] imageBytes = s3Service.downloadFile(imageUrl);
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("이미지 다운로드 및 Base64 인코딩 실패: {}", e.getMessage());
            throw new PhotoUploadFailedException();
        }
    }


    /**
     * 사진 삭제
     *
     * @param photoId 삭제할 사진 ID
     * @param userId  현재 사용자 ID
     */
    @Transactional
    public void deletePhoto(Long photoId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        PhotoAlbum photo = photoAlbumRepository.findById(photoId)
                .orElseThrow(PhotoNotFoundException::new);

        // 같은 커플인지 확인
        if (!photo.getCouple().getId().equals(user.getCouple().getId())) {
            throw new UnauthorizedException();
        }

        try {
            // S3에서 이미지와 3D 모델 파일 삭제
            s3Service.deleteFile(photo.getImageUrl());
            if (photo.getObjectUrl() != null) {
                s3Service.deleteFile(photo.getObjectUrl());
            }

            // DB에서 데이터 삭제
            photoAlbumRepository.delete(photo);

            log.info("사진 삭제 완료 - photoId: {}, coupleId: {}",
                    photoId, photo.getCouple().getId());
        } catch (Exception e) {
            log.error("사진 삭제 중 오류 발생 - photoId: {}", photoId, e);
            throw new PhotoNotFoundException();
        }
    }

    /**
     * 원본 이미지 업로드
     *
     * @param photo 업로드할 사진 파일
     * @return 업로드된 이미지의 URL
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

    /**
     * 파일명에서 확장자를 추출
     *
     * @param filename 파일명
     * @return 추출된 확장자
     */
    private String getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".")))
                .orElse(".jpg");
    }


    /**
     * 사진 조회 (날짜 기준 내림차순)
     * 커플에 속한 두 사용자가 올린 모든 사진을 조회합니다.
     * @param userId 현재 사용자 ID
     * @return 조회된 사진 리스트
     */
    @Transactional(readOnly = true)
    public List<PhotoAlbumResponseDTO> getPhotos(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Couple couple = user.getCouple();
        if (couple == null) {
            throw new CoupleNotFoundException();
        }

        return photoAlbumRepository.findByCoupleIdOrderByPhotoDateDesc(couple.getId())
                .stream()
                .sorted(Comparator.comparing(PhotoAlbum::getPhotoDate).reversed()) //날짜 기준 내림차순 정렬
                .map(photo -> new PhotoAlbumResponseDTO(
                        photo.getId(),
                        photo.getTitle(),
                        photo.getContent(),
                        photo.getPhotoDate(),
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
