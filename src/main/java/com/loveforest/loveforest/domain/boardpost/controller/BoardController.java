package com.loveforest.loveforest.domain.boardpost.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.boardpost.dto.*;
import com.loveforest.loveforest.domain.boardpost.entity.Answer;
import com.loveforest.loveforest.domain.boardpost.entity.DailyTopic;
import com.loveforest.loveforest.domain.boardpost.exception.AnswerNotFoundException;
import com.loveforest.loveforest.domain.boardpost.exception.DailyTopicNotFoundException;
import com.loveforest.loveforest.domain.boardpost.service.AnswerService;
import com.loveforest.loveforest.domain.boardpost.service.CommentService;
import com.loveforest.loveforest.domain.boardpost.service.DailyTopicService;
import com.loveforest.loveforest.domain.user.exception.LoginRequiredException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/topic")
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
            summary = "Daily 토픽 생성",
            description = "로그인한 사용자가 새로운 질문을 생성합니다. 질문은 하루에 하나씩만 존재할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "질문 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청으로 인해 질문 생성 실패"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자가 접근할 수 없습니다.")
    })
    @PostMapping
    public ResponseEntity<DailyTopicResponseDTO> createDailyTopic(
            @Valid @RequestBody DailyTopicRequestDTO dailyTopicRequestDTO,
            @AuthenticationPrincipal LoginInfo loginInfo) {
        log.info("Daily 토픽 생성 요청 - 사용자 ID: {}", loginInfo.getUserId());

        DailyTopic dailyTopic = dailyTopicService.createDailyTopic(dailyTopicRequestDTO.getContent(), dailyTopicRequestDTO.getDate());
        DailyTopicResponseDTO dailyTopicIdResponse = new DailyTopicResponseDTO(dailyTopic);

        log.info("Daily 토픽 생성 성공 - 토픽 ID: {}", dailyTopicIdResponse.getId());
        return ResponseEntity.ok(dailyTopicIdResponse);
    }

    /**
     * Daily 토픽 특정 날짜의 질문 조회
     * */
    @Operation(
            summary = "모든 Daily 토픽 조회",
            description = "로그인한 사용자가 모든 Daily 토픽을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "질문 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 날짜에 질문이 존재하지 않음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자가 접근할 수 없습니다.")
    })
    @GetMapping
    public ResponseEntity<List<DailyTopicResponseDTO>> getDailyTopicByDate(@AuthenticationPrincipal LoginInfo loginInfo) {
        log.info("모든 Daily 토픽 조회 요청 - 사용자 ID: {}", loginInfo.getUserId());

        List<DailyTopicResponseDTO> dailyTopicList = dailyTopicService.getAllDailyTopic();

        log.info("모든 Daily 토픽 조회 성공 - 조회된 토픽 수: {}", dailyTopicList.size());
        return ResponseEntity.ok(dailyTopicList);
    }


    /**
     * Daily 토픽 특정 날짜의 질문 조회
     * */
    @Operation(
            summary = "특정 날짜의 Daily 토픽 조회",
            description = "로그인한 사용자가 특정 날짜에 등록된 질문을 조회합니다. 날짜별로 질문을 확인할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토픽 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 날짜에 질문이 존재하지 않음"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자가 접근할 수 없습니다.")
    })
    @GetMapping("/date/{date}")
    public ResponseEntity<DailyTopic> getAllDailyTopicByDate(
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd 형식)", example = "2023-01-01")
            @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @AuthenticationPrincipal LoginInfo loginInfo) {

        log.info("특정 날짜의 Daily 토픽 조회 요청 - 사용자 ID: {}, 날짜: {}", loginInfo.getUserId(), date);

        DailyTopic dailyTopic = dailyTopicService.getDailyTopicByDate(date);

        log.info("특정 날짜의 Daily 토픽 조회 성공 - 토픽 ID: {}", dailyTopic.getId());
        return ResponseEntity.ok(dailyTopic);
    }

    /**
     * Daily 토픽에 대한 게시글 생성
     * */
    @Operation(summary = "Daily 토픽에 대한 게시글 생성", description = "1일 1질문에 대한 게시글을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "404", description = "해당 토픽이 존재하지 않음")
    })
    @PostMapping("/answer/create")
    public ResponseEntity<AnswerResponseDTO> createAnswer(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @Valid @RequestBody AnswerRequestDTO answerRequestDTO) {

        log.info("게시글 생성 요청 - 사용자 ID: {}, 토픽 ID: {}", loginInfo.getUserId(), answerRequestDTO.getDailyTopicId());

        DailyTopic dailyTopic = dailyTopicService.getDailyTopicById(answerRequestDTO.getDailyTopicId())
                .orElseThrow(DailyTopicNotFoundException::new);

        AnswerResponseDTO answerResponse = answerService.createAnswer(answerRequestDTO, loginInfo.getNickname(), dailyTopic);

        log.info("게시글 생성 성공 - 게시글 ID: {}", answerResponse.getId());
        return ResponseEntity.ok(answerResponse);
    }


    /**
     * 게시글 조회
     * */
    @Operation(summary = "Daily 토픽에 대한 게시글 조회", description = "1일 1토픽에 대해 작성된 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 토픽이 존재하지 않음")
    })
    @GetMapping("/{dailyTopicId}/answers")
    public ResponseEntity<List<AnswerResponseDTO>> getAnswersByDailyTopic(
            @PathVariable("dailyTopicId") Long dailyTopicId,
            @AuthenticationPrincipal LoginInfo loginInfo) {

        log.info("게시글 조회 요청 - 토픽 ID: {}", dailyTopicId);

        DailyTopic dailyTopic = dailyTopicService.getDailyTopicById(dailyTopicId)
                .orElseThrow(DailyTopicNotFoundException::new);
        List<AnswerResponseDTO> answers = answerService.getAnswersByDailyTopic(dailyTopic);

        log.info("게시글 조회 성공 - 토픽 ID: {}, 조회된 게시글 수: {}", dailyTopicId, answers.size());
        return ResponseEntity.ok(answers);
    }

    /**
     * 댓글 생성
     * */
    @Operation(summary = "댓글 생성", description = "특정 게시글에 댓글을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 생성 성공"),
            @ApiResponse(responseCode = "404", description = "해당 게시글이 존재하지 않음")
    })
    @PostMapping("/comment/create")
    public ResponseEntity<CommentResponseDTO> createComment(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @Valid @RequestBody CommentRequestDTO commentRequestDTO) {

        log.info("댓글 생성 요청 - 사용자 ID: {}, 게시글 ID: {}", loginInfo.getUserId(), commentRequestDTO.getAnswerId());

        Answer answer = answerService.getAnswerById(commentRequestDTO.getAnswerId())
                .orElseThrow(AnswerNotFoundException::new);

        CommentResponseDTO commentResponse = commentService.createComment(commentRequestDTO.getContent(), loginInfo.getNickname(), answer);

        log.info("댓글 생성 성공 - 댓글 ID: {}", commentResponse.getId());
        return ResponseEntity.ok(commentResponse);
    }


    /**
     * 답변에 대한 댓글 조회
     * */
    @Operation(summary = "게시글에 대한 댓글 조회", description = "특정 게시글에 달린 댓글 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 게시글이 존재하지 않음")
    })
    @GetMapping("/{answerId}/comments")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByAnswer(
            @PathVariable("answerId") Long answerId,
            @AuthenticationPrincipal LoginInfo loginInfo) { // 인증된 사용자 정보 추가

        log.info("댓글 조회 요청 - 게시글 ID: {}", answerId);

        Answer answer = answerService.getAnswerById(answerId)
                .orElseThrow(AnswerNotFoundException::new);

        List<CommentResponseDTO> comments = commentService.getCommentsByAnswer(answer);

        log.info("댓글 조회 성공 - 게시글 ID: {}, 조회된 댓글 수: {}", answerId, comments.size());
        return ResponseEntity.ok(comments);
    }

    /**
     * 답변 좋아요
     */
    @PostMapping("/answer/{answerId}/like")
    @Operation(summary = "게시글 좋아요", description = "특정 게시글에 좋아요를 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 추가 성공"),
            @ApiResponse(responseCode = "404", description = "해당 게시글을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "중복 좋아요"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ResponseEntity<LikeResponseDTO> likeAnswer(
            @Parameter(description = "좋아요를 추가할 게시글의 ID", example = "1")
            @PathVariable("answerId") Long answerId,
            @AuthenticationPrincipal LoginInfo loginInfo) {

        log.info("게시글 좋아요 요청 - 사용자 ID: {}, 게시글 ID: {}", loginInfo.getUserId(), answerId);

        LikeResponseDTO response = answerService.likeAnswer(answerId, loginInfo.getUserId());

        log.info("게시글 좋아요 추가 성공 - 게시글 ID: {}, 좋아요 상태: {}", answerId, response.isLiked());
        return ResponseEntity.ok(response);
    }

    /**
     * 답변 좋아요 취소
     */
    @PostMapping("/answer/{answerId}/unlike")
    @Operation(summary = "게시글 좋아요 취소", description = "특정 게시글에 추가된 좋아요를 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 취소 성공"),
            @ApiResponse(responseCode = "404", description = "좋아요가 추가되지 않은 게시글"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ResponseEntity<LikeResponseDTO> unlikeAnswer(
            @Parameter(description = "좋아요를 취소할 게시글의 ID", example = "1")
            @PathVariable("answerId") Long answerId,
            @AuthenticationPrincipal LoginInfo loginInfo) {

        log.info("게시글 좋아요 취소 요청 - 사용자 ID: {}, 게시글 ID: {}", loginInfo.getUserId(), answerId);

        LikeResponseDTO response = answerService.unlikeAnswer(answerId, loginInfo.getUserId());

        log.info("게시글 좋아요 취소 성공 - 게시글 ID: {}, 좋아요 상태: {}", answerId, response.isLiked());
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 좋아요
     */
    @PostMapping("/comment/{commentId}/like")
    @Operation(summary = "댓글 좋아요", description = "특정 댓글에 좋아요를 추가합니다. 동일한 사용자가 중복하여 좋아요를 추가할 수 없습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 추가 성공"),
            @ApiResponse(responseCode = "404", description = "해당 댓글을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "중복 좋아요 - 이미 좋아요를 추가한 경우"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ResponseEntity<LikeResponseDTO> likeComment(
            @Parameter(description = "좋아요를 추가할 댓글의 ID", example = "1")
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal LoginInfo loginInfo) {

        log.info("댓글 좋아요 요청 - 사용자 ID: {}, 댓글 ID: {}", loginInfo.getUserId(), commentId);

        LikeResponseDTO response = commentService.likeComment(commentId, loginInfo.getUserId());

        log.info("댓글 좋아요 추가 성공 - 댓글 ID: {}, 좋아요 상태: {}", commentId, response.isLiked());
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 좋아요 취소
     */
    @PostMapping("/comment/{commentId}/unlike")
    @Operation(summary = "댓글 좋아요 취소", description = "특정 댓글에 추가된 좋아요를 취소합니다. 좋아요가 추가된 상태에서만 취소가 가능합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요 취소 성공"),
            @ApiResponse(responseCode = "404", description = "좋아요가 추가되지 않은 댓글"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ResponseEntity<LikeResponseDTO> unlikeComment(
            @Parameter(description = "좋아요를 취소할 댓글의 ID", example = "1")
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal LoginInfo loginInfo) {

        log.info("댓글 좋아요 취소 요청 - 사용자 ID: {}, 댓글 ID: {}", loginInfo.getUserId(), commentId);

        LikeResponseDTO response = commentService.unlikeComment(commentId, loginInfo.getUserId());

        log.info("댓글 좋아요 취소 성공 - 댓글 ID: {}, 좋아요 상태: {}", commentId, response.isLiked());
        return ResponseEntity.ok(response);
    }
}