package com.loveforest.loveforest.domain.boardpost.entity;

import com.loveforest.loveforest.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content; // 댓글 내용

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author; // 댓글 작성자

    @ManyToOne
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer; // 연관된 답변

    @Column(nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now(); // 생성 시간

    public Comment(String content, User author, Answer answer) {
        this.content = content;
        this.author = author;
        this.answer = answer;
    }
}