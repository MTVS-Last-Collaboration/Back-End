package com.loveforest.loveforest.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(byte[] fileData, String extension) {
        String fileName = UUID.randomUUID().toString() + extension;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileData.length);

        // S3에 파일 업로드
        amazonS3.putObject(new PutObjectRequest(
                bucket,
                fileName,
                new ByteArrayInputStream(fileData),
                metadata
        ));

        // 업로드된 파일의 URL 반환
        return amazonS3.getUrl(bucket, fileName).toString();
    }
}
