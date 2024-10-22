package com.loveforest.loveforest.domain.calendar.service;

import com.loveforest.loveforest.domain.calendar.dto.CalendarEventRequestDTO;
import com.loveforest.loveforest.domain.calendar.dto.CalendarEventResponseDTO;
import com.loveforest.loveforest.domain.calendar.entity.CalendarEvent;
import com.loveforest.loveforest.domain.calendar.repository.CalendarEventRepository;
import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private final CalendarEventRepository calendarEventRepository;
    private final CoupleRepository coupleRepository;

    public CalendarService(CalendarEventRepository calendarEventRepository, CoupleRepository coupleRepository) {
        this.calendarEventRepository = calendarEventRepository;
        this.coupleRepository = coupleRepository;
    }

    // 이벤트 추가
    public CalendarEventResponseDTO addEvent(CalendarEventRequestDTO requestDTO) {
        Couple couple = coupleRepository.findById(requestDTO.getCoupleId())
                .orElseThrow(() -> new IllegalArgumentException("해당 커플을 찾을 수 없습니다."));

        CalendarEvent event = new CalendarEvent(couple, requestDTO.getEventName(), requestDTO.getEventDate(), requestDTO.getDescription());
        calendarEventRepository.save(event);

        return mapToResponseDTO(event);
    }

    // 이벤트 조회
    public List<CalendarEventResponseDTO> getEvents(Long coupleId) {
        List<CalendarEvent> events = calendarEventRepository.findByCouple_Id(coupleId);

        return events.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // 이벤트 수정
    public CalendarEventResponseDTO updateEvent(Long eventId, CalendarEventRequestDTO requestDTO) {
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));

        event.updateEvent(requestDTO.getEventName(), requestDTO.getEventDate(), requestDTO.getDescription());
        calendarEventRepository.save(event);

        return mapToResponseDTO(event);
    }

    // 이벤트 삭제
    public void deleteEvent(Long eventId) {
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));

        calendarEventRepository.delete(event);
    }

    private CalendarEventResponseDTO mapToResponseDTO(CalendarEvent event) {
        return new CalendarEventResponseDTO(
                event.getId(),
                event.getEventName(),
                event.getEventDate(),
                event.getDescription()
        );
    }
}