package com.loveforest.loveforest.domain.boardpost.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.boardpost.dto.CommentRequestDTO;
import com.loveforest.loveforest.domain.boardpost.dto.CommentResponseDTO;
import com.loveforest.loveforest.domain.boardpost.entity.Answer;
import com.loveforest.loveforest.domain.boardpost.entity.Comment;
import com.loveforest.loveforest.domain.boardpost.service.AnswerService;
import com.loveforest.loveforest.domain.boardpost.service.CommentService;
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
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "게시판 질문 댓글 API", description = "답변에 대한 댓글을 관리하는 API입니다.")
public class CommentController {

    private final CommentService commentService;
    private final AnswerService answerService;

    /**
     * 댓글 생성
     * */
    @Operation(summary = "댓글 생성", description = "특정 답변에 댓글을 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 생성 성공"),
            @ApiResponse(responseCode = "404", description = "해당 답변이 존재하지 않음")
    })
    @PostMapping
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
    @GetMapping("/{answerId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByAnswer(
            @PathVariable Long answerId) {
        Answer answer = answerService.getAnswerById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 답변입니다."));
        List<CommentResponseDTO> comments = commentService.getCommentsByAnswer(answer);
        return ResponseEntity.ok(comments);
    }
}