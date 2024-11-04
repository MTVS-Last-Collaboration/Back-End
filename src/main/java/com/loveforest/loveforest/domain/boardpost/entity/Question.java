package com.loveforest.loveforest.domain.boardpost.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content; // 질문 내용

    @Column(nullable = false, unique = true)
    private LocalDate date; // 질문 생성 날짜

    public Question(String content, LocalDate date) {
        this.content = content;
        this.date = date;
    }
}