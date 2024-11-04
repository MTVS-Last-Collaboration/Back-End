package com.loveforest.loveforest.domain.boardpost.service;

import com.loveforest.loveforest.domain.boardpost.dto.CommentResponseDTO;
import com.loveforest.loveforest.domain.boardpost.entity.Answer;
import com.loveforest.loveforest.domain.boardpost.entity.Comment;
import com.loveforest.loveforest.domain.boardpost.exception.AnswerNotFoundException;
import com.loveforest.loveforest.domain.boardpost.exception.CommentsNotFoundException;
import com.loveforest.loveforest.domain.boardpost.repository.CommentRepository;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

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
                savedComment.getContent(),
                authorNickname,
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
                        comment.getContent(),
                        comment.getAuthor().getNickname(),
                        comment.getCreatedDate()
                ))
                .collect(Collectors.toList());
    }

    public Optional<Comment> getCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }
}