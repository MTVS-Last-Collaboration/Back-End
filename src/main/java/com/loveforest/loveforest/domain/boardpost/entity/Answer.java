package com.loveforest.loveforest.domain.boardpost.entity;

import com.loveforest.loveforest.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content; // 답변 내용

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author; // 답변 작성자

    @ManyToOne
    @JoinColumn(name = "daily_topic_id", nullable = false)
    private DailyTopic dailyTopic; // 연관된 질문

    @Column(nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now(); // 생성 시간

    @Column(nullable = false)
    private int likeCount = 0; // 좋아요 수 초기값 설정

    public Answer(String content, User author, DailyTopic dailyTopic) {
        this.content = content;
        this.author = author;
        this.dailyTopic = dailyTopic;
    }

    public void incrementLike() {
        this.likeCount++;
    }

    public void decrementLike() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}