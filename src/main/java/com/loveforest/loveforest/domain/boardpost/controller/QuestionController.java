package com.loveforest.loveforest.domain.boardpost.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.boardpost.entity.Question;
import com.loveforest.loveforest.domain.boardpost.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "게시판 API", description = "1일 1질문 게시판 API")
public class QuestionController {

    private final QuestionService questionService;

    @Operation(
            summary = "오늘의 질문 생성",
            description = "로그인한 사용자가 새로운 질문을 생성합니다. 질문은 하루에 하나씩만 존재할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "질문 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청으로 인해 질문 생성 실패"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자가 접근할 수 없습니다.")
    })
    @PostMapping
    public ResponseEntity<Question> createQuestion(
            @Parameter(description = "오늘의 질문 내용", example = "오늘의 목표는 무엇인가요?") @RequestParam String content,
            @AuthenticationPrincipal LoginInfo loginInfo) {

        // 로그인한 사용자 정보 확인
        Long userId = loginInfo.getUserId();
        Question question = questionService.createQuestion(content);
        return ResponseEntity.ok(question);
    }

    @Operation(
            summary = "특정 날짜의 질문 조회",
            description = "로그인한 사용자가 특정 날짜에 등록된 질문을 조회합니다. 날짜별로 질문을 확인할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "질문 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 날짜에 질문이 존재하지 않음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자가 접근할 수 없습니다.")
    })
    @GetMapping("/{date}")
    public ResponseEntity<Question> getQuestionByDate(
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd 형식)", example = "2023-01-01")
            @PathVariable(name = "date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @AuthenticationPrincipal LoginInfo loginInfo) {

        // 로그인한 사용자 정보 확인
        Long userId = loginInfo.getUserId();
        Optional<Question> question = questionService.getQuestionByDate(date);
        return question.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}