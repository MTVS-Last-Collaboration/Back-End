package com.loveforest.loveforest.domain.pet.entity;

public enum AppearanceStage {
    BABY,    // 레벨 1-5
    CHILD,   // 레벨 6-10
    ADOLESCENT, // 레벨 11-15
    FINAL_STAGE; // 레벨 16-20

    public static AppearanceStage fromLevel(int level) {
        if (level >= 16) return FINAL_STAGE;
        if (level >= 11) return ADOLESCENT;
        if (level >= 6) return CHILD;
        return BABY;
    }
}
