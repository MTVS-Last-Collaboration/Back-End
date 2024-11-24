package com.loveforest.loveforest.domain.flower.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StartNewSeedResponseDTO {
    private String message;     // 성공 메시지
    private int moodCount;      // 초기화된 MoodCount
    private String flowerName;  // 꽃 이름
}
