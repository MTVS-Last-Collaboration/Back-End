package com.loveforest.loveforest.domain.boardpost.entity;

import com.loveforest.loveforest.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "comment_like",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"comment_id", "user_id"}
        ))
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Version  // 낙관적 락을 위한 버전 필드 추가
    private Long version;

    public CommentLike(Comment comment, User user) {
        this.comment = comment;
        this.user = user;
    }
}