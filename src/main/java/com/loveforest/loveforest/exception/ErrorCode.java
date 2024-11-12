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
    LOGIN_REQUIRED(HttpStatus.FORBIDDEN, "Login Required", "로그인이 필요합니다.", "USER-004"),

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
    FURNITURE_NOT_FOUND(HttpStatus.NOT_FOUND, "Furniture Not Found", "해당 가구를 찾을 수 없습니다.", "ROOM-002"),
    FURNITURE_OVERLAP(HttpStatus.CONFLICT, "Furniture Overlap", "해당 위치에 이미 다른 가구가 배치되어 있습니다.", "ROOM-003"),
    FURNITURE_LAYOUT_NOT_FOUND(HttpStatus.NOT_FOUND, "Furniture Layout Not Found", "해당 가구 배치를 찾을 수 없습니다.", "ROOM-004"),
    ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "Room Already Exists", "이미 해당 커플의 방이 존재합니다.", "ROOM-005"),
    INVALID_ROOM_CREATION(HttpStatus.BAD_REQUEST, "Invalid Room Creation", "방 생성에 필요한 데이터가 유효하지 않습니다.", "ROOM-006"),
    ROOM_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Room Creation Failed", "방 생성 중 오류가 발생했습니다.", "ROOM-007"),
    FURNITURE_NOT_IN_INVENTORY(HttpStatus.BAD_REQUEST, "Furniture Not In Inventory", "보유하지 않은 가구입니다.", "ROOM-008"),
    WALLPAPER_NOT_FOUND(HttpStatus.NOT_FOUND, "Wallpaper Not Found", "해당 벽지를 찾을 수 없습니다.", "ROOM-009"),
    WALLPAPER_NOT_IN_INVENTORY(HttpStatus.BAD_REQUEST, "Wallpaper Not In Inventory", "보유하지 않은 벽지입니다.", "ROOM-010"),
    FLOOR_NOT_FOUND(HttpStatus.NOT_FOUND, "Floor Not Found", "해당 바닥을 찾을 수 없습니다.", "ROOM-011"),
    FLOOR_NOT_IN_INVENTORY(HttpStatus.BAD_REQUEST, "Floor Not In Inventory", "보유하지 않은 바닥입니다.", "ROOM-012"),
    INVALID_ROOM_ACCESS(HttpStatus.BAD_REQUEST, "Invalid Room Access", "잘못된 방 접근입니다.", "ROOM-013"),

    // Chat 관련 에러
    CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "Chat Not Found", "대화 이력이 존재하지 않습니다.", "CHAT-001"),

    // Board 관련 에러
    DAILY_TOPIC_NOT_FOUND(HttpStatus.NOT_FOUND, "DailyTopic Not Found", "존재하지 않는 일일 토픽입니다.", "TOPIC-001"),
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "Answer Not Found", "존재하지 않는 일일 게시글입니다.", "TOPIC-002"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Comment Not Found", "댓글이 존재하지 않습니다.", "TOPIC-003"),
    DAILY_TOPIC_ALREADY_EXIST(HttpStatus.CONFLICT, "Already DailyTopic Existed", "이미 해당 토픽이 존재합니다.", "TOPIC-004"),
    LIKE_ALREADY_EXIST(HttpStatus.CONFLICT, "Already Like Existed", "이미 좋아요를 눌렀습니다.", "TOPIC-005"),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "Like Not Found", "좋아요가 추가되지 않은 답변입니다.", "TOPIC-006"),
    COMMENT_LIKE_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Comment Like Operation Failed", "댓글 좋아요 작업 처리 중 오류가 발생했습니다.", "COMMENT-007"),

    // Pet 관련 에러
    PET_NOT_FOUND(HttpStatus.NOT_FOUND, "Pet Not Found", "해당 커플에 연동된 팻을 찾을 수 없습니다.", "PET-001"),
    MAX_LEVEL_REACHED(HttpStatus.BAD_REQUEST, "Max Level Reached", "팻의 최대 레벨에 도달했습니다.", "PET-002"),

    // Flower 관련 에러
    MAX_MOOD_COUNT_REACHED(HttpStatus.BAD_REQUEST, "Max Mood Count Reached", "꽃의 최대 기분 카운트를 초과했습니다.", "FLOWER-001"),
    AI_SERVER_ERROR_FLOWER(HttpStatus.INTERNAL_SERVER_ERROR, "AI Server Error", "AI 서버에서 오류가 발생했습니다.", "FLOWER-002"),
    MOOD_ANALYSIS_FAILED(HttpStatus.BAD_REQUEST, "Mood Analysis Failed", "기분 상태를 분석할 수 없습니다.", "FLOWER-003"),

    // Mission 관련 에러 추가
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "Mission Not Found", "해당 미션을 찾을 수 없습니다.", "MISSION-001"),
    MISSION_ALREADY_ANSWERED(HttpStatus.CONFLICT, "Mission Already Answered", "이미 답변한 미션입니다.", "MISSION-002"),
    PREVIOUS_MISSION_INCOMPLETE(HttpStatus.BAD_REQUEST, "Previous Mission Incomplete", "이전 미션이 완료되지 않았습니다.", "MISSION-003"),
    DAILY_MISSION_ALREADY_EXISTS(HttpStatus.CONFLICT, "Daily Mission Already Exists", "해당 날짜에 이미 미션이 존재합니다.", "MISSION-004"),
    MISSION_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Mission Generation Failed", "미션 생성 중 오류가 발생했습니다.", "MISSION-005"),
    AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI Server Error", "AI 서버와의 통신 중 오류가 발생했습니다.", "MISSION-006"),
    MISSION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "Mission Already Completed", "이미 완료된 미션은 수정할 수 없습니다.", "MISSION-007"
    ),

    // Photo 관련 에러
    PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "Photo Not Found", "해당 사진을 찾을 수 없습니다.", "PHOTO-001"),
    PHOTO_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Photo Upload Failed", "사진 업로드에 실패했습니다.", "PHOTO-002"),
    DUPLICATE_PHOTO_POSITION(HttpStatus.CONFLICT, "Duplicate Photo Position", "해당 위치에 이미 사진이 존재합니다.", "PHOTO-003"),
    AI_SERVER_ERROR_PHOTO(HttpStatus.INTERNAL_SERVER_ERROR, "AI Server Error", "AI 서버에서 오류가 발생했습니다.", "PHOTO-004"),

    // Shop 관련 에러
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "Item Not Found", "상품을 찾을 수 없습니다.", "SHOP-001"),
    INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST, "Insufficient Points", "포인트가 부족합니다.", "SHOP-002"),
    INITIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Initialization Failed", "상점 데이터 초기화에 실패했습니다.", "SHOP-003"),
    ITEM_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "Item Not Available", "해당 상품은 현재 구매할 수 없습니다.", "SHOP-004"),
    PURCHASE_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Purchase Processing Failed", "상품 구매 처리 중 오류가 발생했습니다.", "SHOP-005");




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