package com.loveforest.loveforest.domain.boardpost.service;

import com.loveforest.loveforest.domain.boardpost.entity.Question;
import com.loveforest.loveforest.domain.boardpost.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    public Question createQuestion(String content) {
        Question question = new Question(content, LocalDate.now());
        return questionRepository.save(question);
    }

    public Optional<Question> getQuestionByDate(LocalDate date) {
        return questionRepository.findByDate(date);
    }

    public Optional<Question> getQuestionById(Long questionId) {
        return questionRepository.findById(questionId);
    }
}