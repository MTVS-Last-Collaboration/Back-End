package com.loveforest.loveforest.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 메시지 ID

    @Column(name = "sender_id", nullable = false)
    private Long senderId; // 발신자 ID

    @Column(name = "couple_id", nullable = false)
    private Long coupleId; // 커플 ID

    @Column(nullable = false)
    private String message; // 사용자가 보낸 메시지

    private String response; // AI 서버의 응답

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp; // 메시지 전송 시간

    // 기본 생성자
    public ChatMessage() {}

    // 생성자
    public ChatMessage(Long senderId, Long coupleId ,String message, String response) {
        this.senderId = senderId;
        this.coupleId = coupleId;
        this.message = message;
        this.response = response;
        this.timestamp = LocalDateTime.now();
    }

}