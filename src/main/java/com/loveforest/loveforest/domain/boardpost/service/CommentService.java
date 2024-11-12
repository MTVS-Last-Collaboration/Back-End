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
            log.debug("댓글 좋아요 취소 시작 - 댓글 ID: {}, 사용자 ID: {}", commentId, userId);

            // 1. 댓글 엔티티 조회 및 검증
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

            // 2. 좋아요 엔티티 조회 및 검증
            CommentLike commentLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId)
                    .orElseThrow(NotLikedException::new);

            // 3. 트랜잭션 내에서 좋아요 삭제 및 카운트 감소
            commentLikeRepository.delete(commentLike);

            // 4. 댓글의 좋아요 카운트 감소
            comment.decrementLike();
            Comment savedComment = commentRepository.save(comment);

            log.info("댓글 좋아요 취소 완료 - 댓글 ID: {}, 현재 좋아요 수: {}", commentId, savedComment.getLikeCount());

            return new LikeResponseDTO(
                    savedComment.getId(),
                    savedComment.getLikeCount(),
                    false
            );

        } catch (NotLikedException e) {
            log.warn("좋아요를 누르지 않은 댓글 - 댓글 ID: {}, 사용자 ID: {}", commentId, userId);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("댓글을 찾을 수 없음 - 댓글 ID: {}", commentId);
            throw e;
        } catch (Exception e) {
            log.error("댓글 좋아요 취소 중 오류 발생 - 댓글 ID: {}, 오류: {}", commentId, e.getMessage(), e);
            throw new CommentLikeOperationException();
        }
    }

    // 중복 좋아요 확인
    public boolean isCommentLikedByUser(Long commentId, Long userId) {
        return commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
    }
}