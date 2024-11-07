package com.loveforest.loveforest.domain.flower.service;

import com.loveforest.loveforest.domain.flower.dto.FlowerMoodResponseDTO;
import com.loveforest.loveforest.domain.flower.entity.Flower;
import com.loveforest.loveforest.domain.flower.exception.AiServerException;
import com.loveforest.loveforest.domain.flower.exception.MaxMoodCountReachedException;
import com.loveforest.loveforest.domain.flower.exception.MoodAnalysisException;
import com.loveforest.loveforest.domain.flower.repository.FlowerRepository;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.exception.UserNotFoundException;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${ai.flower-url}")
    private String serverUrl;

    @Transactional
    public FlowerMoodResponseDTO analyzeMood(Long userId, byte[] voiceMessage) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Flower flower = flowerRepository.findByUserId(userId)
                .orElseGet(() -> new Flower("My Flower", user));

        // WebClient 인스턴스 생성
        WebClient webClient = webClientBuilder.baseUrl(serverUrl).build();

        // AI 서버로 음성 메시지를 전송하여 기분 상태 분석
        String mood;
        try {
            mood = webClient.post()
                    .bodyValue(voiceMessage)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorMap(WebClientResponseException.class, ex -> {
                        log.error("AI 서버 응답 실패: {}", ex.getMessage());
                        throw new AiServerException();
                    })
                    .block();
        } catch (Exception ex) {
            log.error("기분 상태 분석 중 오류 발생: {}", ex.getMessage());
            throw new MoodAnalysisException();
        }

        // 기분 상태가 '상' 또는 '중'인 경우에만 moodCount 증가
        if ("상".equals(mood) || "중".equals(mood)) {
            flower.incrementMoodCount();  // moodCount 증가
            flower.addPoints(10);         // 포인트 추가
            if (flower.getMoodCount() >= 30) {
                throw new MaxMoodCountReachedException();
            }
        }

        flowerRepository.save(flower);

        return new FlowerMoodResponseDTO(mood);
    }

    @Transactional
    public void setFlowerName(Long userId, String newName) {
        Flower flower = flowerRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);
        flower.updateName(newName);
        flowerRepository.save(flower);
    }

    @Transactional
    public void startNewSeed(Long userId) {
        Flower flower = flowerRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);
        flower.resetMoodCount();
        flowerRepository.save(flower);
    }
}