package com.loveforest.loveforest.domain.chat.service;

import com.loveforest.loveforest.domain.chat.dto.ChatMessageRequestDTO;
import com.loveforest.loveforest.domain.chat.dto.ChatMessageResponseDTO;
import com.loveforest.loveforest.domain.chat.entity.ChatMessage;
import com.loveforest.loveforest.domain.chat.exception.ChatNotFoundException;
import com.loveforest.loveforest.domain.chat.repository.ChatMessageRepository;
import com.loveforest.loveforest.domain.chat.dto.AiResponseDTO;
import com.loveforest.loveforest.exception.common.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final RestTemplate restTemplate;
    private final String aiServerUrl;

    @Autowired
    public ChatService(ChatMessageRepository chatMessageRepository,
                       RestTemplate restTemplate,
                       @Value("${ai.server.url}") String aiServerUrl) { // 설정 파일에서 URL 주입
        this.chatMessageRepository = chatMessageRepository;
        this.restTemplate = restTemplate;
        this.aiServerUrl = aiServerUrl;
    }

    public ChatMessageResponseDTO processMessage(Long senderId, Long coupleId, String message) {

        // 예외 처리: 만약 메시지가 비어있거나 null이라면 예외 발생
        if (message == null || message.trim().isEmpty()) {
            throw new InvalidInputException();
        }

        // AI 서버에 메시지 전송
        String aiResponse = callAiServer(senderId, message, coupleId); // senderId도 포함
        System.out.println("AI 서버 응답" + aiResponse);

        // DB에 메시지 저장
        ChatMessage chatMessage = new ChatMessage(senderId, coupleId, message, aiResponse);
        chatMessageRepository.save(chatMessage); // DB에 저장

        // AI 서버의 응답만을 포함한 DTO 반환
        return new ChatMessageResponseDTO(senderId, coupleId ,aiResponse);
    }


    private String callAiServer(Long senderId, String message, Long coupleId) {
        Long petLevel = 1L;
        ChatMessageRequestDTO requestDTO = new ChatMessageRequestDTO(senderId, message, coupleId, petLevel);
        log.info("Sending POST request to AI server at URL: {} with payload: {}", aiServerUrl, requestDTO);

        // AI 서버에 요청 보내기
        AiResponseDTO aiResponseDTO = restTemplate.postForObject(aiServerUrl + "/chatbot", requestDTO, AiResponseDTO.class);

        if (aiResponseDTO == null || aiResponseDTO.getResponse() == null || aiResponseDTO.getResponse().getAnswer() == null) {
            log.error("AI 서버 응답이 유효하지 않습니다.");
            throw new IllegalStateException("AI 서버 응답이 유효하지 않습니다.");
        }

        return aiResponseDTO.getResponse().getAnswer(); // 실제 응답 메시지 반환
    }


    public List<ChatMessage> getChatHistory(Long coupleId) {
        List<ChatMessage> history = chatMessageRepository.findByCoupleId(coupleId);

        if (history.isEmpty()) {
            throw new ChatNotFoundException();
        }
        return history;
    }
}