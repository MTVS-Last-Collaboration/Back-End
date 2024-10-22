package com.loveforest.loveforest.domain.calendar.repository;

import com.loveforest.loveforest.domain.calendar.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    List<CalendarEvent> findByCouple_Id(Long coupleId);
}