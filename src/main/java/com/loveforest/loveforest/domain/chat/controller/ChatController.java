package com.loveforest.loveforest.domain.chat.controller;

import com.loveforest.loveforest.domain.chat.dto.ChatMessageRequestDTO;
import com.loveforest.loveforest.domain.chat.entity.ChatMessage;
import com.loveforest.loveforest.domain.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat API", description = "Chat 관련 API입니다.")
public class ChatController {

    private final ChatService chatService;

    /**
     * 메시지 전송 API
     *
     * @param request 발신자 ID와 메시지 내용을 담은 DTO
     * @return 처리된 메시지를 반환
     */
    @Operation(summary = "메시지 전송", description = "사용자가 메시지를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메시지가 성공적으로 처리되었습니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChatMessage.class)
            )),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"인증되지 않은 사용자입니다.\"}")
            )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"BadRequest\", \"message\": \"잘못된 요청입니다.\"}")
            ))
    })
    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessageRequestDTO request) {
        ChatMessage chatMessage = chatService.processMessage(request.getSenderId(), request.getMessage());
        return ResponseEntity.ok(chatMessage); // 처리된 메시지 반환
    }


    /**
     * 사용자 대화 이력 조회 API
     *
     * @param userId 사용자 ID
     * @return 해당 사용자의 대화 이력을 반환
     */
    @Operation(summary = "대화 이력 조회", description = "사용자의 대화 이력을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대화 이력이 성공적으로 조회되었습니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChatMessage.class)
            )),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"인증되지 않은 사용자입니다.\"}")
            )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"BadRequest\", \"message\": \"잘못된 요청입니다.\"}")
            ))
    })
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Long userId) {
        List<ChatMessage> history = chatService.getChatHistory(userId);
        return ResponseEntity.ok(history); // 대화 이력 반환
    }
}
