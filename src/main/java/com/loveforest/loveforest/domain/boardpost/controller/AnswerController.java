package com.loveforest.loveforest.domain.boardpost.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.boardpost.dto.AnswerRequestDTO;
import com.loveforest.loveforest.domain.boardpost.dto.AnswerResponseDTO;
import com.loveforest.loveforest.domain.boardpost.entity.Answer;
import com.loveforest.loveforest.domain.boardpost.entity.Question;
import com.loveforest.loveforest.domain.boardpost.service.AnswerService;
import com.loveforest.loveforest.domain.boardpost.service.QuestionService;
import com.loveforest.loveforest.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
@Tag(name = "게시판 답변 API", description = "1일 1질문 게시판 답변을 관리하는 API입니다.")
public class AnswerController {

    private final AnswerService answerService;
    private final QuestionService questionService;

    /**
     * 답변 생성
     * */
    @Operation(summary = "답변 생성", description = "특정 질문에 대한 답변을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "답변 생성 성공"),
            @ApiResponse(responseCode = "404", description = "해당 질문이 존재하지 않음")
    })
    @PostMapping
    public ResponseEntity<AnswerResponseDTO> createAnswer(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @Valid @RequestBody AnswerRequestDTO answerRequestDTO) {
        Question question = questionService.getQuestionById(answerRequestDTO.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문입니다."));

        AnswerResponseDTO answerResponse = answerService.createAnswer(answerRequestDTO, loginInfo.getNickname(), question);
        return ResponseEntity.ok(answerResponse);
    }


    /**
     * 질문에 대한 답변 조회
     * */
    @Operation(summary = "질문에 대한 답변 조회", description = "특정 질문에 대해 작성된 답변 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "답변 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 질문이 존재하지 않음")
    })
    @GetMapping("/{questionId}")
    public ResponseEntity<List<AnswerResponseDTO>> getAnswersByQuestion(
            @PathVariable Long questionId) {
        Question question = questionService.getQuestionById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문입니다."));
        List<AnswerResponseDTO> answers = answerService.getAnswersByQuestion(question);
        return ResponseEntity.ok(answers);
    }
}