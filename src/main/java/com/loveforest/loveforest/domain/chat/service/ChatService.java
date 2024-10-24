package com.loveforest.loveforest.domain.chat.service;

import com.loveforest.loveforest.domain.chat.dto.ChatMessageRequestDTO;
import com.loveforest.loveforest.domain.chat.entity.ChatMessage;
import com.loveforest.loveforest.domain.chat.exception.ChatNotFoundException;
import com.loveforest.loveforest.domain.chat.repository.ChatMessageRepository;
import com.loveforest.loveforest.exception.common.InvalidInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final RestTemplate restTemplate;
    private final String aiServerUrl;

    @Autowired
    public ChatService(ChatMessageRepository chatMessageRepository,
                       RestTemplate restTemplate,
                       @Value("${ai.server-url}") String aiServerUrl) { // 설정 파일에서 URL 주입
        this.chatMessageRepository = chatMessageRepository;
        this.restTemplate = restTemplate;
        this.aiServerUrl = aiServerUrl;
    }

    public ChatMessage processMessage(Long senderId, Long coupleId, String message) {

        // 예외 처리: 만약 메시지가 비어있거나 null이라면 예외 발생
        if (message == null || message.trim().isEmpty()) {
            throw new InvalidInputException();
        }

        // AI 서버에 메시지 전송
        String aiResponse = callAiServer(senderId, message, coupleId); // senderId도 포함

        // DB에 메시지 저장
        ChatMessage chatMessage = new ChatMessage(senderId, coupleId, message, aiResponse);
        return chatMessageRepository.save(chatMessage); // DB에 저장
    }

    private String callAiServer(Long senderId, String message, Long coupleId) {
        // AI 서버에 요청 보내기
        ChatMessageRequestDTO requestDTO = new ChatMessageRequestDTO(senderId, message, coupleId);
        return restTemplate.postForObject(aiServerUrl, requestDTO, String.class);
    }


    public List<ChatMessage> getChatHistory(Long coupleId) {
        List<ChatMessage> history = chatMessageRepository.findByCoupleId(coupleId);

        if (history.isEmpty()) {
            throw new ChatNotFoundException();
        }
        return history;
    }
}