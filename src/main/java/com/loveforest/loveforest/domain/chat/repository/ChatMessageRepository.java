package com.loveforest.loveforest.domain.chat.repository;

import com.loveforest.loveforest.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByCoupleId(Long coupleId);
}
