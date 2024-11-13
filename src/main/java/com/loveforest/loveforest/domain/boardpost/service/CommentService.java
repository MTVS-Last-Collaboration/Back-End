package com.loveforest.loveforest.domain.boardpost.service;

import com.loveforest.loveforest.domain.boardpost.dto.CommentResponseDTO;
import com.loveforest.loveforest.domain.boardpost.dto.LikeResponseDTO;
import com.loveforest.loveforest.domain.boardpost.entity.Answer;
import com.loveforest.loveforest.domain.boardpost.entity.Comment;
import com.loveforest.loveforest.domain.boardpost.entity.CommentLike;
import com.loveforest.loveforest.domain.boardpost.exception.*;
import com.loveforest.loveforest.domain.boardpost.repository.CommentLikeRepository;
import com.loveforest.loveforest.domain.boardpost.repository.CommentRepository;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;

    public CommentResponseDTO createComment(String content, String authorNickname, Answer answer) {
        // 작성자 정보를 기반으로 User 객체를 생성하거나 조회
        User author = userRepository.findByNickname(authorNickname)
                .orElseThrow(AnswerNotFoundException::new);  // 실제 프로젝트에서는 ID로 조회하는 것이 좋습니다.

        // Comment 엔티티 생성 및 저장
        Comment comment = new Comment(content, author, answer);
        Comment savedComment = commentRepository.save(comment);

        // 저장된 Comment 엔티티를 CommentResponseDTO로 변환하여 반환
        return new CommentResponseDTO(
                savedComment.getId(),
                savedComment.getAnswer().getId(),
                savedComment.getContent(),
                authorNickname,
                savedComment.getLikeCount(),
                savedComment.getCreatedDate()
        );
    }

    public List<CommentResponseDTO> getCommentsByAnswer(Answer answer) {
        List<Comment> comments = commentRepository.findByAnswer(answer);

        if (comments.isEmpty()) {
            throw new CommentsNotFoundException(); // 댓글이 없을 경우 예외 발생
        }

        return comments.stream()
                .map(comment -> new CommentResponseDTO(
                        comment.getId(),
                        answer.getId(),
                        comment.getContent(),
                        comment.getAuthor().getNickname(),
                        comment.getLikeCount(),
                        comment.getCreatedDate()
                ))
                .collect(Collectors.toList());
    }

    // 댓글에 대한 좋아요 추가
    @Transactional
    public LikeResponseDTO likeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (isCommentLikedByUser(commentId, userId)) {
            throw new AlreadyLikedException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        CommentLike commentLike = new CommentLike(comment, user);
        commentLikeRepository.save(commentLike);
        comment.incrementLike();

        return new LikeResponseDTO(comment.getId(), comment.getLikeCount(), true);
    }

    // 댓글에 대한 좋아요 취소
    @Transactional
    public LikeResponseDTO unlikeComment(Long commentId, Long userId) {
        try {
            List<CommentLike> likes = commentLikeRepository
                    .findByCommentIdAndUserId(commentId, userId);

            if (likes.isEmpty()) {
                throw new NotLikedException();
            }

            // 중복된 좋아요가 있다면 모두 제거
            commentLikeRepository.deleteAll(likes);

            Comment comment = likes.get(0).getComment();
            comment.decrementLike();
            Comment savedComment = commentRepository.save(comment);

            return new LikeResponseDTO(
                    savedComment.getId(),
                    savedComment.getLikeCount(),
                    false
            );
        } catch (Exception e) {
            log.error("댓글 좋아요 취소 중 오류 발생", e);
            throw new CommentLikeOperationException();
        }
    }

    // 중복 좋아요 확인
    public boolean isCommentLikedByUser(Long commentId, Long userId) {
        return commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
    }
}