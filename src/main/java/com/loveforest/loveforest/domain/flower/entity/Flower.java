package com.loveforest.loveforest.domain.flower.entity;

import com.loveforest.loveforest.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_flower")
@Getter
@NoArgsConstructor
public class Flower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 꽃 이름

    @Column(nullable = false)
    private int moodCount = 0; // 중이상 상태 횟수

    // 음성 메시지 S3 URL 추가
    @Column(name = "voice_url")
    private String voiceUrl;

    @Column(nullable = false)
    private boolean recordComplete = false;  // 녹음 완료 상태

    @Column(nullable = false)
    private boolean listenComplete = false;  // 청취 완료 상태

    @Column
    private LocalDateTime listenedAt;        // 청취 완료 시간

    // 음성 메시지 저장 시간 추가
    @Column(name = "voice_saved_at")
    private LocalDateTime voiceSavedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 각 사용자의 꽃

    public Flower(String name, User user) {
        this.name = name;
        this.user = user;
    }

    public void incrementMoodCount() {
        this.moodCount++;
    }

    public void resetMoodCount() {
        this.moodCount = 0;
    }

    public void updateName(String newName) {
        this.name = newName;
    }

    // 음성 메시지 저장 시
    public void updateVoiceMessage(String url) {
        this.voiceUrl = url;
        this.voiceSavedAt = LocalDateTime.now();
        this.recordComplete = true;      // 녹음 완료 상태로 변경
        this.listenComplete = false;     // 청취 상태 초기화
        this.listenedAt = null;
    }

    // 음성 메시지 청취 완료 시
    public void markAsListened() {
        this.listenComplete = true;
        this.listenedAt = LocalDateTime.now();
    }

    // 음성 메시지 제거
    public void clearVoiceMessage() {
        this.voiceUrl = null;
        this.voiceSavedAt = null;
        this.recordComplete = false;
        this.listenComplete = false;
        this.listenedAt = null;
    }

}