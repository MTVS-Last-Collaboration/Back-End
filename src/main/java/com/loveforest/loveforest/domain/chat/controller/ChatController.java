package com.loveforest.loveforest.domain.chat.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.chat.dto.ChatMessageCRequestDTO;
import com.loveforest.loveforest.domain.chat.dto.ChatMessageDTO;
import com.loveforest.loveforest.domain.chat.dto.ChatMessageRequestDTO;
import com.loveforest.loveforest.domain.chat.dto.ChatMessageResponseDTO;
import com.loveforest.loveforest.domain.chat.entity.ChatMessage;
import com.loveforest.loveforest.domain.chat.service.ChatService;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.exception.LoginRequiredException;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
                    schema = @Schema(implementation = ChatMessageResponseDTO.class)
            )),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"인증되지 않은 사용자입니다.\"}")
            )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"BadRequest\", \"message\": \"잘못된 요청입니다.\"}")
            )),
            @ApiResponse(responseCode = "404", description = "대화 이력이 존재하지 않습니다.", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\": 404, \"error\": \"ChatNotFound\", \"message\": \"대화 이력이 존재하지 않습니다.\"}")
            ))
    })
    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponseDTO> sendMessage(@AuthenticationPrincipal LoginInfo loginInfo, @RequestBody ChatMessageCRequestDTO request) {

        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("메시지 전송 요청 시작 - 사용자 ID: {}, 커플 ID: {}, 메시지 내용: {}", loginInfo.getUserId(), loginInfo.getCoupleId(), request.getMessages());

        ChatMessageResponseDTO responseDTO = chatService.processMessage(loginInfo.getUserId(), loginInfo.getCoupleId(), request.getMessages());

        log.info("메시지 전송 처리 성공 - 사용자 ID: {}, 커플 ID: {}, AI 메시지 내용: {}", loginInfo.getUserId(), loginInfo.getCoupleId(), responseDTO.getAiResponse());
        return ResponseEntity.ok(responseDTO);
    }


    /**
     * 사용자 대화 이력 조회 API
     *
     * @return 해당 사용자의 대화 이력을 반환
     */
    @Operation(summary = "대화 이력 조회", description = "사용자의 대화 이력을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대화 이력이 성공적으로 조회되었습니다.", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChatMessageDTO.class)
            )),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"인증되지 않은 사용자입니다.\"}")
            )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"BadRequest\", \"message\": \"잘못된 요청입니다.\"}")
            )),
            @ApiResponse(responseCode = "404", description = "대화 이력이 존재하지 않습니다.", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\": 404, \"error\": \"ChatNotFound\", \"message\": \"대화 이력이 존재하지 않습니다.\"}")
            ))
    })
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(@AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("대화 이력 조회 요청 시작 - 사용자 ID: {}, 커플 ID: {}", loginInfo.getUserId(), loginInfo.getCoupleId());

        List<ChatMessage> history = chatService.getChatHistory(loginInfo.getCoupleId());
        List<ChatMessageDTO> historyDTO = history.stream()
                .map(ChatMessageDTO::fromEntity)
                .collect(Collectors.toList());

        log.info("대화 이력 조회 성공 - 사용자 ID: {}, 커플 ID: {}, 조회된 메시지 수: {}", loginInfo.getUserId(), loginInfo.getCoupleId(), historyDTO.size());
        return ResponseEntity.ok(historyDTO);
    }
}
