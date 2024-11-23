package com.loveforest.loveforest.domain.room.enums;

import lombok.Getter;

@Getter
public enum RoomStateSource {
    CURRENT("현재 방", "현재 내 방의 상태를 저장한 것입니다."),
    PRESET("프리셋", "서비스에서 제공하는 프리셋 방입니다."),
    SHARED("공유 방", "다른 커플이 공유한 방입니다.");

    private final String title;
    private final String description;

    RoomStateSource(String title, String description) {
        this.title = title;
        this.description = description;
    }
}