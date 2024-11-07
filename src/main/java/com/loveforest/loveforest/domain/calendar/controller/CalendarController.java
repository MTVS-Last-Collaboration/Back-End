package com.loveforest.loveforest.domain.calendar.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.calendar.dto.CalendarEventRequestDTO;
import com.loveforest.loveforest.domain.calendar.dto.CalendarEventResponseDTO;
import com.loveforest.loveforest.domain.calendar.service.CalendarService;
import com.loveforest.loveforest.domain.user.exception.LoginRequiredException;
import com.loveforest.loveforest.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Tag(name = "캘린더 API", description = "사용자가 중요한 날짜와 이벤트를 관리하는 캘린더 관련 API입니다.")
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(
            summary = "이벤트 추가",
            description = "사용자가 특정 커플의 캘린더에 새로운 이벤트를 추가하는 API입니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이벤트가 성공적으로 추가되었습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CalendarEventResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청입니다. 유효하지 않은 커플 ID 또는 이벤트 정보입니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = "{\"status\": 400, \"errorType\": \"BadRequest\", \"message\": \"유효하지 않은 요청입니다.\"}"
                                    )
                            )
                    )
            }
    )
    @PostMapping("/event")
    public ResponseEntity<CalendarEventResponseDTO> addEvent(@AuthenticationPrincipal LoginInfo loginInfo, @RequestBody CalendarEventRequestDTO requestDTO) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }
        log.info("이벤트 추가 요청 시작 - 사용자 ID: {}, 커플 ID: {}", loginInfo.getUserId(), loginInfo.getCoupleId());

        Long coupleId = loginInfo.getCoupleId();
        CalendarEventResponseDTO responseDTO = calendarService.addEvent(coupleId, requestDTO);

        log.info("이벤트 추가 성공 - 이벤트 ID: {}, 제목: {}", responseDTO.getEventId(), responseDTO.getEventName());
        return ResponseEntity.ok(responseDTO);
    }

    // 이벤트 조회
    @Operation(
            summary = "이벤트 목록 조회",
            description = "특정 커플의 캘린더에 등록된 모든 이벤트를 조회하는 API입니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이벤트 목록이 성공적으로 조회되었습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CalendarEventResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "해당 커플의 이벤트를 찾을 수 없습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = "{\"status\": 404, \"errorType\": \"NotFound\", \"message\": \"이벤트를 찾을 수 없습니다.\"}"
                                    )
                            )
                    )
            }
    )
    @GetMapping("/events")
    public ResponseEntity<List<CalendarEventResponseDTO>> getEvents(@AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("이벤트 목록 조회 요청 시작 - 사용자 ID: {}, 커플 ID: {}", loginInfo.getUserId(), loginInfo.getCoupleId());

        Long coupleId = loginInfo.getCoupleId();
        List<CalendarEventResponseDTO> events = calendarService.getEvents(coupleId);

        log.info("이벤트 목록 조회 성공 - 커플 ID: {}, 조회된 이벤트 수: {}", coupleId, events.size());
        return ResponseEntity.ok(events);
    }

    // 이벤트 수정
    @Operation(
            summary = "이벤트 수정",
            description = "사용자가 캘린더에 등록된 이벤트 정보를 수정하는 API입니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이벤트가 성공적으로 수정되었습니다.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "해당 이벤트를 찾을 수 없습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = "{\"status\": 404, \"errorType\": \"NotFound\", \"message\": \"이벤트를 찾을 수 없습니다.\"}"
                                    )
                            )
                    )
            }
    )
    @PutMapping("/event/{eventId}")
    public ResponseEntity<CalendarEventResponseDTO> updateEvent(@AuthenticationPrincipal LoginInfo loginInfo, @PathVariable("eventId") Long eventId, @RequestBody CalendarEventRequestDTO requestDTO) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("이벤트 수정 요청 시작 - 사용자 ID: {}, 이벤트 ID: {}", loginInfo.getUserId(), eventId);

        CalendarEventResponseDTO responseDTO = calendarService.updateEvent(eventId, requestDTO);

        log.info("이벤트 수정 성공 - 이벤트 ID: {}, 새로운 제목: {}", responseDTO.getEventId(), responseDTO.getEventName());
        return ResponseEntity.ok(responseDTO);
    }

    // 이벤트 삭제
    @Operation(
            summary = "이벤트 삭제",
            description = "특정 커플의 캘린더에서 이벤트를 삭제하는 API입니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이벤트가 성공적으로 삭제되었습니다.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "해당 이벤트를 찾을 수 없습니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            value = "{\"status\": 404, \"errorType\": \"NotFound\", \"message\": \"이벤트를 찾을 수 없습니다.\"}"
                                    )
                            )
                    )
            }
    )
    @DeleteMapping("/event/{eventId}")
    public ResponseEntity<Void> deleteEvent(@AuthenticationPrincipal LoginInfo loginInfo, @PathVariable("eventId") Long eventId) {

        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("이벤트 삭제 요청 시작 - 이벤트 ID: {}", eventId);

        calendarService.deleteEvent(eventId);

        log.info("이벤트 삭제 성공 - 이벤트 ID: {}", eventId);
        return ResponseEntity.noContent().build();
    }
}