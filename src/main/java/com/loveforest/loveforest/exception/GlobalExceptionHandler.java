package com.loveforest.loveforest.exception;

import com.loveforest.loveforest.domain.boardpost.exception.AnswerNotFoundException;
import com.loveforest.loveforest.domain.boardpost.exception.DailyTopicNotFoundException;
import com.loveforest.loveforest.domain.chat.exception.ChatNotFoundException;
import com.loveforest.loveforest.domain.couple.exception.CoupleAlreadyExists;
import com.loveforest.loveforest.domain.user.exception.EmailAlreadyExistsException;
import com.loveforest.loveforest.domain.user.exception.InvalidPasswordException;
import com.loveforest.loveforest.exception.common.InvalidInputException;
import com.loveforest.loveforest.exception.common.UnauthorizedException;
import com.loveforest.loveforest.domain.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 400 BadRequest 관련 예외 처리
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInputException(InvalidInputException ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getMessage(), ex.getErrorCode().getCode());
    }

    // 401 Unauthorized 관련 예외 처리
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getMessage(), ex.getErrorCode().getCode());
    }

    // 유저 관련 예외 처리
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChatNotFoundException(ChatNotFoundException ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }

    // 커플 관련 예외 처리
    @ExceptionHandler(CoupleAlreadyExists.class)
    public ResponseEntity<ErrorResponse> handleCoupleAlreadyExists(CoupleAlreadyExists ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }

    // 게시판 관련 예외 처리
    @ExceptionHandler(DailyTopicNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDailyTopicNotFoundException(DailyTopicNotFoundException ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }

    @ExceptionHandler(AnswerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAnswerNotFoundException(AnswerNotFoundException ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }

    // 커스텀 예외 처리 (기타 공통 예외 처리)
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        log.error("커스텀 예외 발생: {}", ex.getMessage(), ex);
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 필드 에러 메시지 수집
        StringBuilder message = new StringBuilder("유효성 검사 오류: ");
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            message.append(String.format("[%s: %s] ", error.getField(), error.getDefaultMessage()));
        }

        log.warn("유효성 검사 실패: {}", message);

        // ErrorResponse 생성 및 반환
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "유효성 검사 오류", message.toString(), "LOVEFOREST-400");
    }

    // 일반적인 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("예상치 못한 예외 발생: ", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류", "서버에 문제가 발생했습니다." , "LOVEFOREST-000");
    }

    // 공통적으로 ErrorResponse를 생성하는 메서드
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String errorType, String message, String Code) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(status.value());
        errorResponse.setErrorType(errorType);
        errorResponse.setMessage(message);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setCode(Code);
        return new ResponseEntity<>(errorResponse, status);
    }
}