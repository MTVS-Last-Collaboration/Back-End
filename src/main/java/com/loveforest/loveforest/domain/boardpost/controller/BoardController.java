package com.loveforest.loveforest.domain.boardpost.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.boardpost.dto.*;
import com.loveforest.loveforest.domain.boardpost.entity.Answer;
import com.loveforest.loveforest.domain.boardpost.entity.DailyTopic;
import com.loveforest.loveforest.domain.boardpost.service.AnswerService;
import com.loveforest.loveforest.domain.boardpost.service.CommentService;
import com.loveforest.loveforest.domain.boardpost.service.DailyTopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
@Tag(name = "게시판 API", description = "1일 1질문 게시판 API")
public class BoardController {

    private final DailyTopicService dailyTopicService;
    private final AnswerService answerService;
    private final CommentService commentService;


    /**
     * Daily 토픽 생성
     * */
    @Operation(
            summary = "Daily 토픽",
            description = "로그인한 사용자가 새로운 질문을 생성합니다. 질문은 하루에 하나씩만 존재할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "질문 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청으로 인해 질문 생성 실패"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자가 접근할 수 없습니다.")
    })
    @PostMapping
    public ResponseEntity<DailyTopicResponseDTO> createDailyTopic(
            @Parameter(description = "오늘의 질문 내용", example = "오늘의 목표는 무엇인가요?") @RequestParam String content,
            @AuthenticationPrincipal LoginInfo loginInfo) {
        DailyTopic dailyTopic = dailyTopicService.createDailyTopic(content);
        DailyTopicResponseDTO dailyTopicIdResponse = new DailyTopicResponseDTO(dailyTopic);
        return ResponseEntity.ok(dailyTopicIdResponse);
    }


    /**
     * Daily 토픽 특정 날짜의 질문 조회
     * */
    @Operation(
            summary = "특정 날짜의 Daily 토픽 조회",
            description = "로그인한 사용자가 특정 날짜에 등록된 질문을 조회합니다. 날짜별로 질문을 확인할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "질문 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 날짜에 질문이 존재하지 않음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자가 접근할 수 없습니다.")
    })
    @GetMapping("/date/{date}")
    public ResponseEntity<DailyTopic> getDailyTopicByDate(
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd 형식)", example = "2023-01-01")
            @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @AuthenticationPrincipal LoginInfo loginInfo) {

        // 로그인한 사용자 정보 확인
        Long userId = loginInfo.getUserId();
        Optional<DailyTopic> dailyTopicId = dailyTopicService.getDailyTopicByDate(date);
        return dailyTopicId.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Daily 토픽에 대한 답변 생성
     * */
    @Operation(summary = "Daily 토픽에 대한 답변 생성", description = "1일 1질문에 대한 답변을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "답변 생성 성공"),
            @ApiResponse(responseCode = "404", description = "해당 질문이 존재하지 않음")
    })
    @PostMapping("/answer/create")
    public ResponseEntity<AnswerResponseDTO> createAnswer(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @Valid @RequestBody AnswerRequestDTO answerRequestDTO) {
        DailyTopic dailyTopic = dailyTopicService.getDailyTopicById(answerRequestDTO.getDailyTopicId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문입니다."));

        AnswerResponseDTO answerResponse = answerService.createAnswer(answerRequestDTO, loginInfo.getNickname(), dailyTopic);
        return ResponseEntity.ok(answerResponse);
    }


    /**
     * 답변 조회
     * */
    @Operation(summary = "Daily 토픽에 대한 답변 조회", description = "1일 1질문에 대해 작성된 답변 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "답변 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 질문이 존재하지 않음")
    })
    @GetMapping("/{dailyTopicId}/answers")
    public ResponseEntity<List<AnswerResponseDTO>> getAnswersByDailyTopic(
            @PathVariable("dailyTopicId") Long dailyTopicId) {
        DailyTopic dailyTopic = dailyTopicService.getDailyTopicById(dailyTopicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문입니다."));
        List<AnswerResponseDTO> answers = answerService.getAnswersByDailyTopic(dailyTopic);
        return ResponseEntity.ok(answers);
    }

    /**
     * 댓글 생성
     * */
    @Operation(summary = "댓글 생성", description = "특정 답변에 댓글을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 생성 성공"),
            @ApiResponse(responseCode = "404", description = "해당 답변이 존재하지 않음")
    })
    @PostMapping("/comment/create")
    public ResponseEntity<CommentResponseDTO> createComment(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @Valid @RequestBody CommentRequestDTO commentRequestDTO) {
        Answer answer = answerService.getAnswerById(commentRequestDTO.getAnswerId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 답변입니다."));

        CommentResponseDTO commentResponse = commentService.createComment(
                commentRequestDTO.getContent(),  // content
                loginInfo.getNickname(),         // 작성자 닉네임
                answer                           // 연관된 답변 객체
        );
        return ResponseEntity.ok(commentResponse);
    }


    /**
     * 답변에 대한 댓글 조회
     * */
    @Operation(summary = "답변에 대한 댓글 조회", description = "특정 답변에 달린 댓글 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 답변이 존재하지 않음")
    })
    @GetMapping("/{answerId}/comments")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByAnswer(
            @PathVariable("answerId") Long answerId,
            @AuthenticationPrincipal LoginInfo loginInfo) { // 인증된 사용자 정보 추가

        // 인증된 사용자인지 확인
        Long userId = loginInfo.getUserId();

        // 답변을 조회하고 없으면 예외 처리
        Answer answer = answerService.getAnswerById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 답변입니다."));

        // 댓글 목록 조회
        List<CommentResponseDTO> comments = commentService.getCommentsByAnswer(answer);
        return ResponseEntity.ok(comments);
    }
}