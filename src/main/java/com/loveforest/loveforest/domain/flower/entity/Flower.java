package com.loveforest.loveforest.domain.flower.entity;

import com.loveforest.loveforest.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 각 사용자의 꽃

    @Column(nullable = false)
    private int points = 0; // 커플 포인트

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

    public void addPoints(int points) {
        this.points += points;
    }
}