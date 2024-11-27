package com.loveforest.loveforest.domain.room.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 방 관련 작업의 결과를 반환하는 공통 응답 DTO
 * JsonInclude를 사용하여 null 값은 JSON 응답에서 제외
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "방 작업 결과 응답 DTO")
public class RoomOperationResponseDTO {

    @Schema(description = "작업 결과 메시지", example = "방 상태가 성공적으로 저장되었습니다.")
    private final String message;

    @Schema(description = "작업 수행 시간", example = "2024-03-19T15:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    @Schema(description = "추가 응답 데이터")
    private final Map<String, Object> data;

    /**
     * 기본 성공 응답 생성
     */
    public static RoomOperationResponseDTO success(String message) {
        return new RoomOperationResponseDTO(
                message,
                LocalDateTime.now(),
                null
        );
    }

    /**
     * 데이터를 포함한 성공 응답 생성
     */
    public static RoomOperationResponseDTO success(String message, Map<String, Object> data) {
        return new RoomOperationResponseDTO(
                message,
                LocalDateTime.now(),
                data
        );
    }


    /**
     * 빌더 패턴을 사용한 응답 생성을 위한 유틸리티 클래스
     */
    public static class ResponseBuilder {
        private final Map<String, Object> data = new HashMap<>();
        private final String message;

        public ResponseBuilder(String message) {
            this.message = message;
        }

        /**
         * 응답에 추가 데이터를 포함
         * @param key 데이터 키
         * @param value 데이터 값
         * @return ResponseBuilder
         */
        public ResponseBuilder addData(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        /**
         * 최종 응답 DTO 생성
         * @return RoomOperationResponseDTO
         */
        public RoomOperationResponseDTO build() {
            return RoomOperationResponseDTO.success(message, data);
        }
    }

    /**
     * 응답 빌더 생성
     * @param message 응답 메시지
     * @return ResponseBuilder
     */
    public static ResponseBuilder builder(String message) {
        return new ResponseBuilder(message);
    }

    /**
     * 방 상태 저장 응답 생성을 위한 유틸리티 메서드
     */
    public static RoomOperationResponseDTO forSaveState(String thumbnailUrl) {
        ResponseBuilder builder = new ResponseBuilder("방 상태가 성공적으로 저장되었습니다.")
                .addData("savedAt", LocalDateTime.now());

        if (thumbnailUrl != null) {
            builder.addData("thumbnailUrl", thumbnailUrl);
        }

        return builder.build();
    }

    /**
     * 프리셋 적용 응답 생성을 위한 유틸리티 메서드
     */
    public static RoomOperationResponseDTO forPresetApply(Long roomId, Long presetId) {
        return new ResponseBuilder("프리셋이 성공적으로 적용되었습니다.")
                .addData("roomId", roomId)
                .addData("presetId", presetId)
                .addData("appliedAt", LocalDateTime.now())
                .build();
    }

    /**
     * 컬렉션 방 상태 적용 응답 생성을 위한 유틸리티 메서드
     */
    public static RoomOperationResponseDTO forCollectionApply(Long collectionRoomId) {
        return new ResponseBuilder("저장된 방 상태가 성공적으로 적용되었습니다.")
                .addData("collectionRoomId", collectionRoomId)
                .addData("appliedAt", LocalDateTime.now())
                .build();
    }
}