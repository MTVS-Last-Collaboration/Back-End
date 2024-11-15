package com.loveforest.loveforest.domain.boardpost.service;

import com.loveforest.loveforest.domain.boardpost.dto.AnswerRequestDTO;
import com.loveforest.loveforest.domain.boardpost.dto.AnswerResponseDTO;
import com.loveforest.loveforest.domain.boardpost.dto.LikeResponseDTO;
import com.loveforest.loveforest.domain.boardpost.entity.Answer;
import com.loveforest.loveforest.domain.boardpost.entity.AnswerLike;
import com.loveforest.loveforest.domain.boardpost.entity.DailyTopic;
import com.loveforest.loveforest.domain.boardpost.exception.AlreadyLikedException;
import com.loveforest.loveforest.domain.boardpost.exception.AnswerNotFoundException;
import com.loveforest.loveforest.domain.boardpost.exception.NotLikedException;
import com.loveforest.loveforest.domain.boardpost.repository.AnswerLikeRepository;
import com.loveforest.loveforest.domain.boardpost.repository.AnswerRepository;
import com.loveforest.loveforest.domain.boardpost.repository.CommentLikeRepository;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final AnswerLikeRepository answerLikeRepository;
    private final CommentLikeRepository commentLikeRepository;

    public AnswerResponseDTO createAnswer(AnswerRequestDTO answerRequestDTO, String nickname, DailyTopic dailyTopic) {

        User author = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        // Answer 엔티티 생성
        Answer answer = new Answer(answerRequestDTO.getTitle() ,answerRequestDTO.getContent(), author, dailyTopic);
        Answer savedAnswer = answerRepository.save(answer);

        // AnswerResponseDTO로 변환하여 반환
        return new AnswerResponseDTO(
                savedAnswer.getId(),
                savedAnswer.getTitle(),
                savedAnswer.getContent(),
                savedAnswer.getAuthor().getNickname(),
                savedAnswer.getLikeCount(),
                savedAnswer.getCreatedDate()
        );
    }

    @Transactional
    public void deleteAnswer(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(AnswerNotFoundException::new);

        answer.getComments().forEach(comment -> {
            commentLikeRepository.deleteAll(comment.getLikes());
        });

        answerRepository.delete(answer);
    }

    public List<AnswerResponseDTO> getAnswersByDailyTopic(DailyTopic dailyTopic) {
        // Answer 엔티티 리스트를 AnswerResponseDTO 리스트로 변환하여 반환
        return answerRepository.findByDailyTopic(dailyTopic).stream()
                .map(answer -> new AnswerResponseDTO(
                        answer.getId(),
                        answer.getTitle(),
                        answer.getContent(),
                        answer.getAuthor().getNickname(), // 작성자 닉네임
                        answer.getLikeCount(),
                        answer.getCreatedDate()
                ))
                .collect(Collectors.toList());
    }

    public Optional<Answer> getAnswerById(Long answerId) {
        return answerRepository.findById(answerId);
    }

    // 답변에 대한 좋아요 추가
    @Transactional
    public LikeResponseDTO likeAnswer(Long answerId, Long userId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(AnswerNotFoundException::new);

        if (isAnswerLikedByUser(answerId, userId)) {
            throw new AlreadyLikedException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        AnswerLike answerLike = new AnswerLike(answer, user);
        answerLikeRepository.save(answerLike);
        answer.incrementLike();

        return new LikeResponseDTO(answer.getId(), answer.getLikeCount(), false);
    }

    // 답변에 대한 좋아요 취소
    @Transactional
    public LikeResponseDTO unlikeAnswer(Long answerId, Long userId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(AnswerNotFoundException::new);

        AnswerLike answerLike = answerLikeRepository.findByAnswerIdAndUserId(answerId, userId)
                .orElseThrow(NotLikedException::new);

        answerLikeRepository.delete(answerLike);
        answer.decrementLike();
        return new LikeResponseDTO(answer.getId(), answer.getLikeCount(), false);
    }

    // 중복 좋아요 확인
    public boolean isAnswerLikedByUser(Long answerId, Long userId) {
        return answerLikeRepository.existsByAnswerIdAndUserId(answerId, userId);
    }
}