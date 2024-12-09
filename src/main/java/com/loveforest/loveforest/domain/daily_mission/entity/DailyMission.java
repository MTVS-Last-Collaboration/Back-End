package com.loveforest.loveforest.domain.daily_mission.entity;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
@Entity
@Table(name = "daily_mission",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"couple_id", "mission_date"},
                        name = "uk_daily_mission_couple_date"
                )
        })
@Getter
@NoArgsConstructor
public class DailyMission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer missionNumber;

    @Column(name = "mission_date", nullable = false)
    private LocalDate missionDate;

    @Column(nullable = false)
    private String missionContent;

    @Column
    private String partner1Mood;

    @Column(length = 1000)
    private String partner1Answer;

    @Column
    private String partner2Mood;

    @Column(length = 1000)
    private String partner2Answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id")
    private Couple couple;

    @Column(nullable = false)
    private boolean isCompleted = false;

    // 생성자
    public DailyMission(Integer missionNumber, LocalDate missionDate, String missionContent, Couple couple) {
        this.missionNumber = missionNumber;
        this.missionDate = missionDate;
        this.missionContent = missionContent;
        this.couple = couple;
        this.partner1Mood = "null";
        this.partner1Answer = "null";
        this.partner2Mood = "null";
        this.partner2Answer = "null";
    }

    // 답변 업데이트 메서드
    public void updateAnswer(String mood, String answer, boolean isPartner1) {
        if (isPartner1) {
            log.debug("파트너1 답변 업데이트 - 이전 mood: {}, answer: {}", this.partner1Mood, this.partner1Answer);
            this.partner1Mood = mood;
            this.partner1Answer = answer;
            log.debug("파트너1 답변 업데이트 완료 - 새로운 mood: {}, answer: {}", mood, answer);
        } else {
            log.debug("파트너2 답변 업데이트 - 이전 mood: {}, answer: {}", this.partner2Mood, this.partner2Answer);
            this.partner2Mood = mood;
            this.partner2Answer = answer;
            log.debug("파트너2 답변 업데이트 완료 - 새로운 mood: {}, answer: {}", mood, answer);
        }

        checkCompletion();
    }

    // 미션 완료 체크
    private void checkCompletion() {
        if (!this.partner1Answer.equals("null") && !this.partner2Answer.equals("null")) {
            this.isCompleted = true;
            this.couple.addPoints(50);  // 미션 완료 시 포인트 추가
            log.info("미션 완료 처리 - 커플 포인트 50 추가됨");
        }
    }
}
