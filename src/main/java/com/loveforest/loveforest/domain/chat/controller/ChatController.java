package com.loveforest.loveforest.domain.chat.controller;

import com.loveforest.loveforest.domain.chat.dto.ChatMessageRequestDTO;
import com.loveforest.loveforest.domain.chat.entity.ChatMessage;
import com.loveforest.loveforest.domain.chat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessageRequestDTO request) {
        ChatMessage chatMessage = chatService.processMessage(request.getSenderId(), request.getMessage());
        return ResponseEntity.ok(chatMessage); // 처리된 메시지 반환
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Long userId) {
        List<ChatMessage> history = chatService.getChatHistory(userId);
        return ResponseEntity.ok(history); // 대화 이력 반환
    }
}
