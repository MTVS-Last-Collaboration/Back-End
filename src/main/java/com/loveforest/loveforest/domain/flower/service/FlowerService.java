package com.loveforest.loveforest.domain.flower.service;

import com.loveforest.loveforest.domain.flower.dto.FlowerMoodResponseDTO;
import com.loveforest.loveforest.domain.flower.entity.Flower;
import com.loveforest.loveforest.domain.flower.exception.MaxMoodCountReachedException;
import com.loveforest.loveforest.domain.flower.repository.FlowerRepository;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.exception.UserNotFoundException;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlowerService {

    private final FlowerRepository flowerRepository;
    private final UserRepository userRepository;

    @Transactional
    public FlowerMoodResponseDTO analyzeMood(Long userId, byte[] voiceMessage) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Flower flower = flowerRepository.findByUserId(userId)
                .orElseGet(() -> new Flower("My Flower", user));

        // AI 분석을 통해 기분 상태 가져오기 (예: mock result)
        String mood = "상"; // 실제 구현 시 AI 서버로 보냄
        flower.incrementMoodCount();

        if (mood.equals("상") || mood.equals("중")) {
            flower.addPoints(10);
            if (flower.getMoodCount() >= 30) {
                throw new MaxMoodCountReachedException();
            }
        }

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