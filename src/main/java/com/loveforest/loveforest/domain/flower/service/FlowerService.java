package com.loveforest.loveforest.domain.flower.service;

import com.loveforest.loveforest.domain.flower.dto.FlowerMoodResponseDTO;
import com.loveforest.loveforest.domain.flower.dto.VoiceAnalysisRequestDTO;
import com.loveforest.loveforest.domain.flower.entity.Flower;
import com.loveforest.loveforest.domain.flower.exception.AiServerFlowerException;
import com.loveforest.loveforest.domain.flower.exception.FlowerNotFoundException;
import com.loveforest.loveforest.domain.flower.exception.MaxMoodCountReachedException;
import com.loveforest.loveforest.domain.flower.exception.MoodAnalysisException;
import com.loveforest.loveforest.domain.flower.repository.FlowerRepository;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.exception.UserNotFoundException;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import com.loveforest.loveforest.exception.ErrorCode;
import com.loveforest.loveforest.exception.common.InvalidInputException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlowerService {

    private final FlowerRepository flowerRepository;
    private final UserRepository userRepository;
    private final WebClient.Builder webClientBuilder; // WebClient.Builder 주입
    private static final int MAX_FLOWER_NAME_LENGTH = 50;

    @Value("${ai.server.url}")
    private String serverUrl;

    @Transactional
    public FlowerMoodResponseDTO analyzeMood(Long userId, String base64VoiceMessage) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Flower flower = flowerRepository.findByUserId(userId)
                .orElseGet(() -> new Flower("My Flower", user));

        // WebClient 인스턴스 생성
        WebClient webClient = webClientBuilder.baseUrl(serverUrl).build();

        // AI 서버로 음성 메시지를 전송하여 기분 상태 분석
        String mood;
        try {
            VoiceAnalysisRequestDTO requestDTO = new VoiceAnalysisRequestDTO(base64VoiceMessage);
            mood = webClient.post()
                    .uri(serverUrl + "/analyze_sentiment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestDTO)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorMap(WebClientResponseException.class, ex -> {
                        log.error("AI 서버 응답 실패: {}", ex.getMessage());
                        throw new AiServerFlowerException();
                    })
                    .block();
        } catch (Exception ex) {
            log.error("기분 상태 분석 중 오류 발생: {}", ex.getMessage());
            throw new MoodAnalysisException();
        }

        // 기분 상태가 '상' 또는 '중'인 경우에만 moodCount 증가
        if ("긍정".equals(mood) || "중립".equals(mood)) {
            flower.incrementMoodCount();  // moodCount 증가
            flower.getUser().getCouple().addPoints(10);       // 커플 포인트 추가
            if (flower.getMoodCount() >= 30) {
                throw new MaxMoodCountReachedException();
            }
        }

        flowerRepository.save(flower);

        return new FlowerMoodResponseDTO(mood, user.getNickname());
    }

    @Transactional
    public void setFlowerName(Long userId, String newName) {
        validateFlowerName(newName);

        Flower flower = flowerRepository.findByUserId(userId)
                .orElseThrow(FlowerNotFoundException::new);

        flower.updateName(newName);
        flowerRepository.save(flower);
    }

    private void validateFlowerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException(ErrorCode.INVALID_FLOWER_NAME);
        }
        if (name.length() > MAX_FLOWER_NAME_LENGTH) {
            throw new InvalidInputException(ErrorCode.INVALID_FLOWER_NAME_LENGTH);
        }
    }

    @Transactional
    public void startNewSeed(Long userId) {
        Flower flower = flowerRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);
        flower.resetMoodCount();
        flowerRepository.save(flower);
    }
}