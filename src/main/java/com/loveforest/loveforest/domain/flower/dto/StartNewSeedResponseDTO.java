package com.loveforest.loveforest.domain.flower.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StartNewSeedResponseDTO {
    private String message;     // 성공 메시지
    private int moodCount;      // 초기화된 MoodCount
    private String flowerName;  // 꽃 이름
    private boolean recordComplete;  // 녹음 완료 상태
    private boolean listenComplete;  // 청취 완료 상태
}
