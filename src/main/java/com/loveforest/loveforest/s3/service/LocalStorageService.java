package com.loveforest.loveforest.s3.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocalStorageService {

    private static final String STORAGE_FOLDER = "s3";

    @PostConstruct
    public void init() {
        try {
            Path storagePath = Paths.get(STORAGE_FOLDER).toAbsolutePath();
            Files.createDirectories(storagePath);
            log.info("저장소 디렉토리 생성 완료: {}", storagePath);
        } catch (IOException e) {
            log.error("저장소 디렉토리 생성 실패", e);
            throw new RuntimeException("저장소 디렉토리를 생성할 수 없습니다!", e);
        }
    }

    public String uploadFile(byte[] fileData, String extension, String contentType, long contentLength) {
        String fileName = UUID.randomUUID().toString() + extension;

        try {
            Path filePath = Paths.get(STORAGE_FOLDER, fileName).toAbsolutePath();
            Files.write(filePath, fileData);
            log.info("파일 저장 성공: {}", fileName);
            return fileName;
        } catch (IOException e) {
            log.error("파일 저장 실패", e);
            throw new RuntimeException("파일 저장에 실패했습니다: " + fileName, e);
        }
    }

    public byte[] downloadFile(String fileName) {
        try {
            Path filePath = Paths.get(STORAGE_FOLDER, fileName).toAbsolutePath();
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("파일 읽기 실패: {}", fileName, e);
            throw new RuntimeException("파일 읽기에 실패했습니다: " + fileName, e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(STORAGE_FOLDER, fileName).toAbsolutePath();
            Files.deleteIfExists(filePath);
            log.info("파일 삭제 성공: {}", fileName);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", fileName, e);
            throw new RuntimeException("파일 삭제에 실패했습니다: " + fileName, e);
        }
    }
}