package com.loveforest.loveforest.domain.boardpost.repository;

import com.loveforest.loveforest.domain.boardpost.entity.Answer;
import com.loveforest.loveforest.domain.boardpost.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAnswer(Answer answer); // 특정 답변에 대한 댓글 조회
}