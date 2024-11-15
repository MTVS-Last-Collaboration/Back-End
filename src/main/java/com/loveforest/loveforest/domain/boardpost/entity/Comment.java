package com.loveforest.loveforest.domain.boardpost.entity;

import com.loveforest.loveforest.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer; // 연관된 답변

    @Column(nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now(); // 생성 시간

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommentLike> likes = new ArrayList<>();

    @Column(nullable = false)
    private int likeCount = 0; // 좋아요 수 초기값 설정

    public Comment(String content, User author, Answer answer) {
        this.content = content;
        this.author = author;
        this.answer = answer;
    }

    public void incrementLike() {
        this.likeCount++;
    }

    @Transactional
    public void decrementLike() {
        synchronized (this) {
            if (this.likeCount > 0) {
                this.likeCount--;
            }
        }
    }
}