package com.loveforest.loveforest.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 공통 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", "서버에 문제가 발생했습니다.", "LOVEFOREST-000"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "Invalid Input", "입력값이 유효하지 않습니다.", "LOVEFOREST-400"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized", "인증에 실패하였습니다.", "LOVEFOREST-401"),

    // User 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User Not Found", "사용자를 찾을 수 없습니다.", "USER-001"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Duplicated Email", "이미 가입된 이메일입니다.", "USER-002"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "Invalid Password", "비밀번호가 유효하지 않습니다.", "USER-003"),

    // Token 관련 에러
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token", "유효하지 않은 리프레시 토큰입니다.", "TOKEN-001"),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Expired Access Token", "액세스 토큰이 만료되었습니다.", "TOKEN-002"),

    // Couple 관련 에러
    COUPLE_NOT_FOUND(HttpStatus.NOT_FOUND, "Couple Not Found", "해당 커플을 찾을 수 없습니다.", "COUPLE-001"),
    COUPLE_CODE_ALREADY_USED(HttpStatus.CONFLICT, "Couple Code Already Used", "이미 사용 중인 커플 코드입니다.", "COUPLE-002"),
    INVALID_COUPLE_CODE(HttpStatus.BAD_REQUEST, "Invalid Couple Code", "유효하지 않은 커플 코드입니다.", "COUPLE-003"),
    COUPLE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Already Couple Existed", "이미 커플이 성사된 상태입니다. 커플 코드를 다시 생성할 수 없습니다.", "COUPLE-004"),

    // Room 관련 에러
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "Room Not Found", "해당 커플에 대한 방을 찾을 수 없습니다.", "ROOM-001"),

    // Chat 관련 에러
    CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "Chat Not Found", "대화 이력이 존재하지 않습니다.", "CHAT-001"),

    // Board 관련 에러
    DAILY_TOPIC_NOT_FOUND(HttpStatus.NOT_FOUND, "DailyTopic Not Found", "존재하지 않는 일일 토픽입니다.", "TOPIC-001"),
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "Answer Not Found", "존재하지 않는 일일 게시글입니다.", "TOPIC-002"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Comment Not Found", "댓글이 존재하지 않습니다.", "TOPIC-003"),
    DAILY_TOPIC_ALREADY_EXIST(HttpStatus.CONFLICT, "Already DailyTopic Existed", "이미 해당 토픽이 존재합니다.", "TOPIC-004");



    private final HttpStatus status;
    private final String errorType;
    private final String description;
    private final String code;

    ErrorCode(HttpStatus status, String errorType, String description, String code) {
        this.status = status;
        this.errorType = errorType;
        this.description = description;
        this.code = code;
    }
}