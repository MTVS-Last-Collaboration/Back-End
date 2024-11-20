package com.loveforest.loveforest.s3.service;

import com.loveforest.loveforest.domain.photoAlbum.exception.PhotoUploadFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URL;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 파일 업로드 메서드 (MultipartFile용)
     */
    public String uploadFile(byte[] fileData, String extension, String contentType, long contentLength) {
        String fileName = UUID.randomUUID().toString() + extension;

        try {
            PutObjectRequest putObjRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(contentType)  // 실제 파일의 Content-Type 사용
                    .contentLength(contentLength)  // 파일 크기 설정
                    .build();

            s3Client.putObject(putObjRequest, RequestBody.fromBytes(fileData));

            byte[] uploadedData = s3Client.getObjectAsBytes(builder -> builder
                            .bucket(bucket)
                            .key(fileName))
                    .asByteArray();

            if (!java.util.Arrays.equals(fileData, uploadedData)) {
                log.error("S3 업로드 데이터 검증 실패: 업로드된 데이터와 원본이 일치하지 않음");
                throw new PhotoUploadFailedException();
            }

            GetUrlRequest urlRequest = GetUrlRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            URL url = s3Client.utilities().getUrl(urlRequest);
            return url.toString();

        } catch (S3Exception e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            throw new PhotoUploadFailedException();
        }
    }

    public byte[] downloadFile(String fileUrl) {
        String fileName = extractFileNameFromUrl(fileUrl);

        try {
            return s3Client.getObjectAsBytes(builder -> builder
                            .bucket(bucket)
                            .key(fileName))
                    .asByteArray();
        } catch (Exception e) {
            log.error("파일 다운로드 실패: {}", e.getMessage());
            throw new RuntimeException("파일 다운로드에 실패했습니다.");
        }
    }

    /**
     * 파일 삭제 메서드
     */
    public void deleteFile(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            log.info("파일 삭제 완료: {}", fileName);
        } catch (S3Exception e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
            throw new RuntimeException("파일 삭제에 실패했습니다.");
        }
    }

    /**
     * URL에서 파일명 추출
     */
    private String extractFileNameFromUrl(String fileUrl) {
        try {
            return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        } catch (Exception e) {
            log.error("URL에서 파일명 추출 실패: {}", fileUrl);
            throw new IllegalArgumentException("잘못된 파일 URL입니다.");
        }
    }

    /**
     * Content Type 설정
     */
    private String determineContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".mp3" -> "audio/mpeg";
            case ".wav" -> "audio/wav";
            case ".m4a" -> "audio/mp4";
            default -> "application/octet-stream";
        };
    }
}
