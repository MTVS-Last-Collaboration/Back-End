package com.loveforest.loveforest.domain.calendar.entity;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "tbl_calendar_event")
@Getter
@NoArgsConstructor
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "icon_number", nullable = true)
    private int iconNumber;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "description")
    private String description;

    // 생성자
    public CalendarEvent(Couple couple, String eventName, int iconNumber,LocalDate eventDate, String description) {
        this.couple = couple;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.iconNumber = iconNumber;
        this.description = description;
    }

    // 업데이트 메서드
    public void updateEvent(String eventName, int iconNumber,LocalDate eventDate, String description) {
        this.eventName = eventName;
        this.iconNumber = iconNumber;
        this.eventDate = eventDate;
        this.description = description;
    }
}