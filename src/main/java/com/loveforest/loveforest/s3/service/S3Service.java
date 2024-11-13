package com.loveforest.loveforest.s3.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.loveforest.loveforest.domain.photoAlbum.exception.PhotoUploadFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 파일 업로드 메서드
     * @param fileData 업로드할 파일 데이터
     * @param extension 파일 확장자
     * @return 업로드된 파일의 URL
     */
    public String uploadFile(byte[] fileData, String extension) {
        String fileName = UUID.randomUUID().toString() + extension;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileData.length);

        try {
            // S3에 파일 업로드
            amazonS3.putObject(new PutObjectRequest(
                    bucket,
                    fileName,
                    new ByteArrayInputStream(fileData),
                    metadata
            ));

            // 업로드된 파일의 URL 반환
            return amazonS3.getUrl(bucket, fileName).toString();

        } catch (AmazonServiceException e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            throw new PhotoUploadFailedException();
        }
    }

    /**
     * 파일 삭제 메서드
     * @param fileUrl 삭제할 파일의 URL
     */
    public void deleteFile(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            amazonS3.deleteObject(bucket, fileName);
            log.info("파일 삭제 완료: {}", fileName);
        } catch (AmazonServiceException e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
            throw new RuntimeException("파일 삭제에 실패했습니다.");
        }
    }

    /**
     * URL에서 파일명 추출
     * @param fileUrl S3 파일 URL
     * @return 파일명
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
     * 파일 URL 생성
     * @param fileName 파일명
     * @return 파일 URL
     */
    public String getFileUrl(String fileName) {
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    /**
     * Presigned URL 생성 (보안이 필요한 경우)
     * @param fileName 파일명
     * @param expirationMinutes URL 만료 시간(분)
     * @return Presigned URL
     */
    public String generatePresignedUrl(String fileName, int expirationMinutes) {
        try {
            Date expiration = new Date();
            expiration.setTime(expiration.getTime() + (expirationMinutes * 60 * 1000));

            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucket, fileName)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);

            URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
            return url.toString();
        } catch (AmazonServiceException e) {
            log.error("Presigned URL 생성 실패: {}", e.getMessage());
            throw new RuntimeException("URL 생성에 실패했습니다.");
        }
    }

    /**
     * 파일 존재 여부 확인
     * @param fileName 확인할 파일명
     * @return 존재 여부
     */
    public boolean doesFileExist(String fileName) {
        try {
            return amazonS3.doesObjectExist(bucket, fileName);
        } catch (AmazonServiceException e) {
            log.error("파일 존재 여부 확인 실패: {}", e.getMessage());
            throw new RuntimeException("파일 확인에 실패했습니다.");
        }
    }

    /**
     * 파일 메타데이터 조회
     * @param fileName 조회할 파일명
     * @return ObjectMetadata
     */
    public ObjectMetadata getFileMetadata(String fileName) {
        try {
            return amazonS3.getObjectMetadata(bucket, fileName);
        } catch (AmazonServiceException e) {
            log.error("메타데이터 조회 실패: {}", e.getMessage());
            throw new RuntimeException("메타데이터 조회에 실패했습니다.");
        }
    }

    /**
     * 파일 복사
     * @param sourceKey 원본 파일 키
     * @param destinationKey 대상 파일 키
     */
    public void copyFile(String sourceKey, String destinationKey) {
        try {
            CopyObjectRequest copyObjectRequest = new CopyObjectRequest(
                    bucket, sourceKey, bucket, destinationKey);
            amazonS3.copyObject(copyObjectRequest);
            log.info("파일 복사 완료: {} -> {}", sourceKey, destinationKey);
        } catch (AmazonServiceException e) {
            log.error("파일 복사 실패: {}", e.getMessage());
            throw new RuntimeException("파일 복사에 실패했습니다.");
        }
    }

    /**
     * Content Type 설정
     * @param extension 파일 확장자
     * @return Content Type
     */
    private String determineContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".zip" -> "application/zip";
            default -> "application/octet-stream";
        };
    }
}
