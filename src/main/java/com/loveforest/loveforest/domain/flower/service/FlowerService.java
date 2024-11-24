package com.loveforest.loveforest.domain.flower.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loveforest.loveforest.domain.couple.exception.CoupleNotFoundException;
import com.loveforest.loveforest.domain.flower.dto.FlowerMoodResponseDTO;
import com.loveforest.loveforest.domain.flower.dto.StartNewSeedResponseDTO;
import com.loveforest.loveforest.domain.flower.dto.VoiceAnalysisRequestDTO;
import com.loveforest.loveforest.domain.flower.dto.VoiceMessageStatusDTO;
import com.loveforest.loveforest.domain.flower.entity.Flower;
import com.loveforest.loveforest.domain.flower.exception.*;
import com.loveforest.loveforest.domain.flower.repository.FlowerRepository;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.exception.UserNotFoundException;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import com.loveforest.loveforest.exception.ErrorCode;
import com.loveforest.loveforest.exception.common.InvalidInputException;
import com.loveforest.loveforest.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlowerService {

    private final FlowerRepository flowerRepository;
    private final UserRepository userRepository;
    private final WebClient.Builder webClientBuilder; // WebClient.Builder 주입
    private static final int MAX_FLOWER_NAME_LENGTH = 50;
    private final S3Service s3Service;

    @Value("${ai.server.url}")
    private String serverUrl;

    /**
     * 사용자에게 새로운 꽃을 생성
     *
     * @param user 사용자 정보
     */
    @Transactional
    public void createFlowerForUser(User user) {
        // 이미 꽃이 있는지 확인
        if (flowerRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }

        Flower flower = new Flower("My Flower", user); // 기본 이름 설정
        flowerRepository.save(flower);
    }

    /**
     * 사용자의 음성 데이터를 분석하여 기분 상태를 반환
     *
     * @param userId    사용자 ID
     * @param voiceFile 업로드된 음성 파일
     * @return 분석된 기분 상태와 사용자 닉네임이 포함된 DTO
     */
    @Transactional
    public FlowerMoodResponseDTO analyzeMood(Long userId, MultipartFile voiceFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Flower flower = flowerRepository.findByUserId(userId)
                .orElseGet(() -> new Flower("My Flower", user));

        // AI 서버에 전송할 데이터 준비
        VoiceAnalysisRequestDTO voiceData = new VoiceAnalysisRequestDTO();
        try {
            // 파일을 Base64로 인코딩 (AI 서버와의 통신용)
            voiceData.setVoiceData(Base64.getEncoder().encodeToString(voiceFile.getBytes()));
        } catch (IOException e) {
            log.error("음성 파일 읽기 실패: {}", e.getMessage());
            throw new VoiceMessageUploadFailedException();
        }

        // AI 서버 분석 요청 및 처리
        String mood = analyzeWithAIServer(voiceData);

        // 기분 상태 업데이트
        flower.updateMoodState(mood);


        // 기분 상태가 '상' 또는 '중'인 경우에만 moodCount 증가
        if ("긍정".equals(mood) || "중립".equals(mood)) {
            try {
                // 음성 메시지 저장
                saveVoiceMessage(userId, voiceFile);

                // moodCount 증가 및 포인트 추가
                flower.incrementMoodCount();
                flower.getUser().getCouple().addPoints(10);

                if (flower.getMoodCount() >= 30) {
                    throw new MaxMoodCountReachedException();
                }
            } catch (Exception e) {
                log.error("긍정 응답 처리 중 오류 발생: {}", e.getMessage());
                throw e;
            }
        }

        flowerRepository.save(flower);
        log.info("기분 분석 완료 - 사용자: {}, 기분: {}, 음성저장: {}",
                user.getNickname(), mood, "긍정".equals(mood));

        return new FlowerMoodResponseDTO(mood, user.getNickname());
    }

    /**
     * AI 서버와 통신하여 기분 상태 분석
     *
     * @param voiceData AI 서버로 전송할 음성 데이터
     * @return 분석된 기분 상태 (긍정/중립/부정 등)
     */
    private String analyzeWithAIServer(VoiceAnalysisRequestDTO voiceData) {
        WebClient webClient = webClientBuilder.baseUrl(serverUrl).build();

        try {
            String rawResponse = webClient.post()
                    .uri("/analyze_sentiment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(voiceData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorMap(WebClientResponseException.class, ex -> {
                        log.error("AI 서버 응답 실패: {}", ex.getMessage());
                        throw new AiServerFlowerException();
                    })
                    .block();
            // 응답 JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(rawResponse);
            String mood = rootNode.path("mood").asText();

            // 만약 중첩된 JSON 문자열이 있다면 다시 파싱
            if (mood.startsWith("{") && mood.endsWith("}")) {
                JsonNode nestedNode = objectMapper.readTree(mood);
                return nestedNode.path("mood").asText();
            }

            return mood;
        } catch (Exception ex) {
            log.error("기분 상태 분석 중 오류 발생: {}", ex.getMessage());
            throw new MoodAnalysisException();
        }
    }

    /**
     * 사용자 꽃 이름 변경
     *
     * @param userId  사용자 ID
     * @param newName 새로운 꽃 이름
     */
    @Transactional
    public void setFlowerName(Long userId, String newName) {
        validateFlowerName(newName);

        Flower flower = flowerRepository.findByUserId(userId)
                .orElseThrow(FlowerNotFoundException::new);

        flower.updateName(newName);
        flowerRepository.save(flower);
    }

    /**
     * 꽃 이름 유효성 검증
     *
     * @param name 새로운 꽃 이름
     */
    private void validateFlowerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException(ErrorCode.INVALID_FLOWER_NAME);
        }
        if (name.length() > MAX_FLOWER_NAME_LENGTH) {
            throw new InvalidInputException(ErrorCode.INVALID_FLOWER_NAME_LENGTH);
        }
    }


    /**
     * 사용자 꽃의 기분 카운트 초기화 (새로운 씨앗 시작)
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public StartNewSeedResponseDTO startNewSeed(Long userId) {
        // 사용자와 꽃 데이터 조회
        Flower flower = flowerRepository.findByUserId(userId)
                .orElseThrow(FlowerNotFoundException::new);

        // 상태 초기화
        flower.resetMoodCount();
        flowerRepository.save(flower);

        log.info("새로운 씨앗 시작 - 사용자 ID: {}, 초기화된 MoodCount: {}", userId, flower.getMoodCount());

        // 응답 DTO 생성
        return new StartNewSeedResponseDTO(
                "새로운 씨앗 키우기가 완료되었습니다.",
                flower.getMoodCount(),
                flower.getName()
        );
    }

    /**
     * 음성 메시지 저장
     *
     * @param userId    사용자 ID
     * @param voiceFile 업로드된 음성 파일
     */
    @Transactional
    public void saveVoiceMessage(Long userId, MultipartFile voiceFile) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 커플 확인
        if (user.getCouple() == null) {
            throw new CoupleNotFoundException();
        }

        Flower flower = flowerRepository.findByUserId(userId)
                .orElseThrow(FlowerNotFoundException::new);


        try {
            // 기존 음성 파일이 있다면 삭제
            if (flower.getVoiceUrl() != null) {
                s3Service.deleteFile(flower.getVoiceUrl());
            }

            // 새로운 음성 파일 업로드
            String voiceUrl = s3Service.uploadFile(
                    voiceFile.getBytes(),
                    getExtension(voiceFile.getOriginalFilename()),
                    voiceFile.getContentType(),
                    voiceFile.getSize()
            );

            // 꽃 상태 업데이트
            flower.updateVoiceMessage(voiceUrl);
            flowerRepository.save(flower);

            log.info("음성 메시지 저장 완료 - 사용자: {}, URL: {}", userId, voiceUrl);
        } catch (IOException e) {
            log.error("음성 파일 처리 실패", e);
            throw new VoiceMessageUploadFailedException();
        }
    }

    private String getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".")))
                .orElse(".m4a");  // 기본 확장자 설정
    }

    /**
     * 음성 메시지 URL 가져오기
     *
     * @param userId 사용자 ID
     * @return 저장된 음성 메시지 URL
     */
    @Transactional
    public String getPartnerVoiceMessage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getCouple() == null) {
            throw new CoupleNotFoundException();
        }

        // 파트너의 Flower 조회
        Flower partnerFlower = flowerRepository.findByUser_Couple_IdAndUserIdNot(
                        user.getCouple().getId(), userId)
                .orElseThrow(FlowerNotFoundException::new);

        // 녹음이 완료되지 않았거나 URL이 없는 경우
        if (!partnerFlower.isRecordComplete() || partnerFlower.getVoiceUrl() == null) {
            throw new VoiceMessageNotFoundException();
        }

        // 청취 완료 처리
        partnerFlower.markAsListened();
        flowerRepository.save(partnerFlower);

        return partnerFlower.getVoiceUrl();
    }

    @Transactional(readOnly = true)
    public VoiceMessageStatusDTO getCoupleVoiceMessageStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getCouple() == null) {
            throw new CoupleNotFoundException();
        }

        // 파트너의 Flower 조회
        Flower partnerFlower = flowerRepository.findByUser_Couple_IdAndUserIdNot(
                        user.getCouple().getId(), userId)
                .orElseThrow(FlowerNotFoundException::new);

        Flower myFlower = flowerRepository.findByUserId(userId)
                .orElseThrow(FlowerNotFoundException::new);

        return new VoiceMessageStatusDTO(
                partnerFlower.isRecordComplete(),
                partnerFlower.isListenComplete(),
                partnerFlower.getVoiceSavedAt(),
                partnerFlower.getListenedAt(),
                partnerFlower.getMoodCount(),
                partnerFlower.getName(),
                myFlower.isRecordComplete(),
                myFlower.isListenComplete(),
                myFlower.getVoiceSavedAt(),
                myFlower.getListenedAt(),
                myFlower.getMoodCount(),
                myFlower.getName()
        );
    }

    /**
     * 만료된 음성 메시지 삭제 (매일 자정 실행)
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteExpiredVoiceMessages() {
        log.info("자정 음성메시지 삭제 작업 시작");

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        List<Flower> flowers = flowerRepository.findAllByVoiceUrlIsNotNull();

        for (Flower flower : flowers) {
            if (flower.getVoiceSavedAt().isBefore(yesterday)) {
                // S3에서 파일 삭제
                s3Service.deleteFile(flower.getVoiceUrl());
                // 엔티티에서 URL 제거
                flower.clearVoiceMessage();
                flowerRepository.save(flower);

                log.info("음성메시지 삭제 완료 - 꽃 ID: {}", flower.getId());
            }
        }
        log.info("자정 음성메시지 삭제 작업 완료");
    }
}