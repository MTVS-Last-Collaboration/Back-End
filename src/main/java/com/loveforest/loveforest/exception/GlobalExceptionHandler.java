package com.loveforest.loveforest.exception;

import com.loveforest.loveforest.domain.auth.jwt.exception.InvalidAccessTokenException;
import com.loveforest.loveforest.domain.auth.jwt.exception.InvalidRefreshTokenException;
import com.loveforest.loveforest.domain.boardpost.exception.*;
import com.loveforest.loveforest.domain.chat.exception.ChatNotFoundException;
import com.loveforest.loveforest.domain.couple.exception.CoupleAlreadyExists;
import com.loveforest.loveforest.domain.couple.exception.CoupleCodeAlreadyUsedException;
import com.loveforest.loveforest.domain.couple.exception.CoupleNotFoundException;
import com.loveforest.loveforest.domain.daily_mission.exception.MissionAlreadyAnsweredException;
import com.loveforest.loveforest.domain.daily_mission.exception.MissionNotFoundException;
import com.loveforest.loveforest.domain.daily_mission.exception.PreviousMissionIncompleteException;
import com.loveforest.loveforest.domain.flower.exception.*;
import com.loveforest.loveforest.domain.pet.exception.MaxLevelReachedException;
import com.loveforest.loveforest.domain.pet.exception.PetNotFoundException;
import com.loveforest.loveforest.domain.photoAlbum.dto.ApiResponseDTO;
import com.loveforest.loveforest.domain.photoAlbum.exception.DuplicatePhotoPositionException;
import com.loveforest.loveforest.domain.photoAlbum.exception.Photo3DConvertFailedException;
import com.loveforest.loveforest.domain.photoAlbum.exception.PhotoNotFoundException;
import com.loveforest.loveforest.domain.photoAlbum.exception.PhotoUploadFailedException;
import com.loveforest.loveforest.domain.room.exception.*;
import com.loveforest.loveforest.domain.user.exception.EmailAlreadyExistsException;
import com.loveforest.loveforest.domain.user.exception.InvalidPasswordException;
import com.loveforest.loveforest.domain.user.exception.LoginRequiredException;
import com.loveforest.loveforest.exception.common.InvalidInputException;
import com.loveforest.loveforest.exception.common.UnauthorizedException;
import com.loveforest.loveforest.domain.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
        log.error("잘못된 입력값: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getMessage(),
                ex.getErrorCode().getCode()
        );
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

    @ExceptionHandler(LoginRequiredException.class)
    public ResponseEntity<ErrorResponse> handleLoginRequiredException(LoginRequiredException ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }

    // 커플 관련 예외 처리
    @ExceptionHandler(CoupleAlreadyExists.class)
    public ResponseEntity<ErrorResponse> handleCoupleAlreadyExists(CoupleAlreadyExists ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }

    // 사진 관련 예외 처리
    @ExceptionHandler(DuplicatePhotoPositionException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePhotoPositionException(DuplicatePhotoPositionException ex) {
        log.error("중복된 사진 위치 - {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    @ExceptionHandler(PhotoUploadFailedException.class)
    public ResponseEntity<ApiResponseDTO<ErrorResponse>> handlePhotoUploadFailedException(
            PhotoUploadFailedException e) {
        log.error("사진 업로드 실패", e);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("사진 업로드에 실패했습니다.");
        errorResponse.setTimestamp(LocalDateTime.now());
        return ResponseEntity.badRequest()
                .body(ApiResponseDTO.success("사진 업로드에 실패했습니다.", errorResponse));
    }

    @ExceptionHandler(PhotoNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<ErrorResponse>> handlePhotoNotFoundException(
            PhotoNotFoundException e) {
        log.error("사진을 찾을 수 없음", e);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage("사진을 찾을 수 없습니다.");
        errorResponse.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.success("사진을 찾을 수 없습니다.", errorResponse));
    }

    @ExceptionHandler(Photo3DConvertFailedException.class)
    public ResponseEntity<ApiResponseDTO<ErrorResponse>> handlePhoto3DConvertFailedException(
            Photo3DConvertFailedException e) {
        log.error("3D 변환 실패", e);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        errorResponse.setMessage("3D 변환 실패했습니다.");
        errorResponse.setTimestamp(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponseDTO.success("3D 변환 실패했습니다.", errorResponse));
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

    @ExceptionHandler(AlreadyLikedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyLikedException(AlreadyLikedException ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }

    @ExceptionHandler(NotLikedException.class)
    public ResponseEntity<ErrorResponse> handleNotLikedException(NotLikedException ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }

    // 댓글 좋아요 관련 예외 처리 추가
    @ExceptionHandler(CommentLikeOperationException.class)
    public ResponseEntity<ErrorResponse> handleCommentLikeOperationException(CommentLikeOperationException ex) {
        log.error("댓글 좋아요 작업 처리 중 오류 발생: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getCode(),
                ex.getErrorCode().getDescription()
        );
    }

    // Optimistic Lock 관련 예외 처리 추가
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        log.error("동시성 제어 오류 발생: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Concurrent Modification Error",
                "다른 사용자가 동시에 수정을 시도했습니다. 다시 시도해주세요.",
                "COMMENT-008"
        );
    }

    // 펫 관련 예외 처리
    @ExceptionHandler(PetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePetNotFoundException(PetNotFoundException ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }

    @ExceptionHandler(MaxLevelReachedException.class)
    public ResponseEntity<ErrorResponse> handleMaxLevelReachedException(MaxLevelReachedException ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(), ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }


    // Flower 관련 예외 처리
    @ExceptionHandler(AiServerFlowerException.class)
    public ResponseEntity<ErrorResponse> handleAiServerException(AiServerFlowerException ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }

    @ExceptionHandler(MoodAnalysisException.class)
    public ResponseEntity<ErrorResponse> handleMoodAnalysisException(MoodAnalysisException ex) {
        return buildErrorResponse(ex.getErrorCode().getStatus(), ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(), ex.getErrorCode().getCode());
    }

    @ExceptionHandler(MaxMoodCountReachedException.class)
    public ResponseEntity<ErrorResponse> handleMaxMoodCountReachedException(MaxMoodCountReachedException ex) {
        log.warn("최대 기분 카운트 도달: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    @ExceptionHandler(FlowerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFlowerNotFoundException(FlowerNotFoundException ex) {
        log.error("꽃을 찾을 수 없음: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    @ExceptionHandler(VoiceMessageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVoiceMessageNotFoundException(
            VoiceMessageNotFoundException ex) {
        log.error("음성 메시지를 찾을 수 없음: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }


    // JWT 토큰 관련 예외 처리 추가
    @ExceptionHandler(InvalidAccessTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAccessTokenException(InvalidAccessTokenException ex) {
        log.error("유효하지 않은 액세스 토큰: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshTokenException(InvalidRefreshTokenException ex) {
        log.error("유효하지 않은 리프레시 토큰: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    // Room 관련 예외 처리 추가
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoomNotFoundException(RoomNotFoundException ex) {
        log.error("방을 찾을 수 없음: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    @ExceptionHandler(FurnitureLayoutNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFurnitureLayoutNotFoundException(FurnitureLayoutNotFoundException ex) {
        log.error("가구 배치를 찾을 수 없음: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    @ExceptionHandler(FurnitureNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFurnitureNotFoundException(FurnitureNotFoundException ex) {
        log.error("가구를 찾을 수 없음: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    @ExceptionHandler(FurnitureOverlapException.class)
    public ResponseEntity<ErrorResponse> handleFurnitureOverlapException(FurnitureOverlapException ex) {
        log.error("가구 배치 중복: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    @ExceptionHandler(RoomAlreadyExistsException .class)
    public ResponseEntity<ErrorResponse> handleRoomAlreadyExistsException(RoomAlreadyExistsException ex) {
        log.warn("방 중복 생성 시도 발생: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    @ExceptionHandler(InvalidRoomCreationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRoomCreationException(InvalidRoomCreationException ex) {
        log.warn("잘못된 방 생성 요청: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    @ExceptionHandler(RoomCreationFailedException.class)
    public ResponseEntity<ErrorResponse> handleRoomCreationFailedException(RoomCreationFailedException ex) {
        log.error("방 생성 중 시스템 오류 발생: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    // 미션 관련 예외 처리 추가
    @ExceptionHandler(MissionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMissionNotFoundException(MissionNotFoundException ex) {
        log.error("미션을 찾을 수 없음: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    @ExceptionHandler(MissionAlreadyAnsweredException.class)
    public ResponseEntity<ErrorResponse> handleMissionAlreadyAnsweredException(MissionAlreadyAnsweredException ex) {
        log.error("이미 답변한 미션: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    @ExceptionHandler(PreviousMissionIncompleteException.class)
    public ResponseEntity<ErrorResponse> handlePreviousMissionIncompleteException(PreviousMissionIncompleteException ex) {
        log.error("이전 미션 미완료: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    // Couple 관련 추가 예외 처리
    @ExceptionHandler(CoupleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCoupleNotFoundException(CoupleNotFoundException ex) {
        log.error("커플을 찾을 수 없음: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
    }

    @ExceptionHandler(CoupleCodeAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleCoupleCodeAlreadyUsedException(CoupleCodeAlreadyUsedException ex) {
        log.error("이미 사용 중인 커플 코드: {}", ex.getMessage());
        return buildErrorResponse(
                ex.getErrorCode().getStatus(),
                ex.getErrorCode().getErrorType(),
                ex.getErrorCode().getDescription(),
                ex.getErrorCode().getCode()
        );
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