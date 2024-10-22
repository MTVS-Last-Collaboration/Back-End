package com.loveforest.loveforest.domain.calendar.controller;

import com.loveforest.loveforest.domain.calendar.dto.CalendarEventRequestDTO;
import com.loveforest.loveforest.domain.calendar.dto.CalendarEventResponseDTO;
import com.loveforest.loveforest.domain.calendar.service.CalendarService;
import com.loveforest.loveforest.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                            content = @Content(mediaType = "application/json")
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
    public ResponseEntity<CalendarEventResponseDTO> addEvent(@RequestBody CalendarEventRequestDTO requestDTO) {
        CalendarEventResponseDTO responseDTO = calendarService.addEvent(requestDTO);
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
    @GetMapping("/events/{coupleId}")
    public ResponseEntity<List<CalendarEventResponseDTO>> getEvents(@PathVariable Long coupleId) {
        List<CalendarEventResponseDTO> events = calendarService.getEvents(coupleId);
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
    public ResponseEntity<CalendarEventResponseDTO> updateEvent(@PathVariable Long eventId, @RequestBody CalendarEventRequestDTO requestDTO) {
        CalendarEventResponseDTO responseDTO = calendarService.updateEvent(eventId, requestDTO);
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
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        calendarService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}