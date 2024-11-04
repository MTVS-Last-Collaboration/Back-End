package com.loveforest.loveforest.domain.boardpost.service;

import com.loveforest.loveforest.domain.boardpost.dto.AnswerRequestDTO;
import com.loveforest.loveforest.domain.boardpost.dto.AnswerResponseDTO;
import com.loveforest.loveforest.domain.boardpost.entity.Answer;
import com.loveforest.loveforest.domain.boardpost.entity.Question;
import com.loveforest.loveforest.domain.boardpost.repository.AnswerRepository;
import com.loveforest.loveforest.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;

    public AnswerResponseDTO createAnswer(AnswerRequestDTO answerRequestDTO, String nickname, Question question) {
        // Answer 엔티티 생성
        Answer answer = new Answer(answerRequestDTO.getContent(), new User(nickname), question);
        Answer savedAnswer = answerRepository.save(answer);

        // AnswerResponseDTO로 변환하여 반환
        return new AnswerResponseDTO(
                savedAnswer.getId(),
                savedAnswer.getContent(),
                savedAnswer.getAuthor().getNickname(),
                savedAnswer.getCreatedDate()
        );
    }

    public List<AnswerResponseDTO> getAnswersByQuestion(Question question) {
        // Answer 엔티티 리스트를 AnswerResponseDTO 리스트로 변환하여 반환
        return answerRepository.findByQuestion(question).stream()
                .map(answer -> new AnswerResponseDTO(
                        answer.getId(),
                        answer.getContent(),
                        answer.getAuthor().getNickname(), // 작성자 닉네임
                        answer.getCreatedDate()
                ))
                .collect(Collectors.toList());
    }

    public Optional<Answer> getAnswerById(Long answerId) {
        return answerRepository.findById(answerId);
    }
}