package com.loveforest.loveforest.domain.calendar.controller;

import com.loveforest.loveforest.domain.calendar.dto.CalendarEventRequestDTO;
import com.loveforest.loveforest.domain.calendar.dto.CalendarEventResponseDTO;
import com.loveforest.loveforest.domain.calendar.service.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    // 이벤트 추가
    @PostMapping("/event")
    public ResponseEntity<CalendarEventResponseDTO> addEvent(@RequestBody CalendarEventRequestDTO requestDTO) {
        CalendarEventResponseDTO responseDTO = calendarService.addEvent(requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    // 이벤트 조회
    @GetMapping("/events/{coupleId}")
    public ResponseEntity<List<CalendarEventResponseDTO>> getEvents(@PathVariable Long coupleId) {
        List<CalendarEventResponseDTO> events = calendarService.getEvents(coupleId);
        return ResponseEntity.ok(events);
    }

    // 이벤트 수정
    @PutMapping("/event/{eventId}")
    public ResponseEntity<CalendarEventResponseDTO> updateEvent(@PathVariable Long eventId, @RequestBody CalendarEventRequestDTO requestDTO) {
        CalendarEventResponseDTO responseDTO = calendarService.updateEvent(eventId, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    // 이벤트 삭제
    @DeleteMapping("/event/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        calendarService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}