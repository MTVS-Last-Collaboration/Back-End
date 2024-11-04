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
    @JoinColumn(name = "question_id", nullable = false)
    private Question question; // 연관된 질문

    @Column(nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now(); // 생성 시간

    public Answer(String content, User author, Question question) {
        this.content = content;
        this.author = author;
        this.question = question;
    }
}