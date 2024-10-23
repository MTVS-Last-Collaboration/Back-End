package com.loveforest.loveforest.domain.chat.service;

import com.loveforest.loveforest.domain.chat.dto.ChatMessageRequestDTO;
import com.loveforest.loveforest.domain.chat.entity.ChatMessage;
import com.loveforest.loveforest.domain.chat.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public ChatService(ChatMessageRepository chatMessageRepository, RestTemplate restTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.restTemplate = restTemplate;
    }

    public ChatMessage processMessage(Long senderId, String message) {
        // AI 서버에 메시지 전송
        String aiResponse = callAiServer(senderId, message); // senderId도 포함

        // DB에 메시지 저장
        ChatMessage chatMessage = new ChatMessage(senderId, message, aiResponse);
        return chatMessageRepository.save(chatMessage); // DB에 저장
    }

    private String callAiServer(Long senderId, String message) {
        String aiServerUrl = "http://ai-server-url/api/respond"; // AI 서버 URL
        // AI 서버에 요청 보내기
        ChatMessageRequestDTO requestDTO = new ChatMessageRequestDTO(senderId, message);
        return restTemplate.postForObject(aiServerUrl, requestDTO, String.class);
    }


    public List<ChatMessage> getChatHistory(Long userId) {
        return chatMessageRepository.findBySenderIdOrReceiverId(userId, userId);
    }
}