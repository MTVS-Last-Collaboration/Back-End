package com.loveforest.loveforest.domain.boardpost.repository;

import com.loveforest.loveforest.domain.boardpost.entity.AnswerLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnswerLikeRepository extends JpaRepository<AnswerLike, Long> {
    Optional<AnswerLike> findByAnswerIdAndUserId(Long answerId, Long userId);

    boolean existsByAnswerIdAndUserId(Long answerId, Long userId);
}
