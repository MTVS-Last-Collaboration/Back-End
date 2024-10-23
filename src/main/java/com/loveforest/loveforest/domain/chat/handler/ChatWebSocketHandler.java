package com.loveforest.loveforest.domain.chat.handler;

import com.loveforest.loveforest.domain.chat.dto.ChatMessageRequestDTO;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final RestTemplate restTemplate;

    public ChatWebSocketHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Long senderId = (Long) session.getAttributes().get("userId"); // 세션에서 사용자 ID 추출

        // AI 서버와 통신하여 응답 생성
        String aiResponse = callAiServer(senderId, payload);
        session.sendMessage(new TextMessage(aiResponse));
    }

    private String callAiServer(Long senderId, String message) {
        // AI 서버와 통신하는 로직 (RestTemplate 사용)
        String aiServerUrl = "http://ai-server-url/api/respond"; // AI 서버 URL
        ChatMessageRequestDTO requestDTO = new ChatMessageRequestDTO(senderId, message);
        return restTemplate.postForObject(aiServerUrl, requestDTO, String.class);
    }
}