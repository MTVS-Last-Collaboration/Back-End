package com.loveforest.loveforest.domain.flower.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.flower.dto.FlowerMoodResponseDTO;
import com.loveforest.loveforest.domain.flower.dto.FlowerRequestDTO;
import com.loveforest.loveforest.domain.flower.dto.VoiceAnalysisRequestDTO;
import com.loveforest.loveforest.domain.flower.dto.VoiceMessageStatusDTO;
import com.loveforest.loveforest.domain.flower.service.FlowerService;
import com.loveforest.loveforest.domain.user.exception.LoginRequiredException;
import com.loveforest.loveforest.exception.ErrorCode;
import com.loveforest.loveforest.exception.ErrorResponse;
import com.loveforest.loveforest.exception.common.InvalidInputException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/flower")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "꽃 API", description = "사용자의 꽃 관리와 기분 상태 분석을 위한 API")
public class FlowerController {

    private final FlowerService flowerService;

    /**
     * 사용자의 기분 상태 분석
     *
     */
    @Operation(summary = "사용자의 기분 상태 분석",
            description = "사용자의 음성 메시지를 분석하여 기분 상태(긍정, 중립, 부정)를 반환합니다. 긍정일 경우 음성이 저장됩니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "기분 상태 분석 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FlowerMoodResponseDTO.class),
                            examples = @ExampleObject(value =
                                    "{\"mood\": \"긍정\", \"nickname\": \"사용자닉네임\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (비어있는 파일 또는 잘못된 파일 형식)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value =
                                    "{\"status\": 400, \"errorType\": \"BadRequest\", \"message\": \"잘못된 음성 파일입니다.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (AI 서버 통신 실패 또는 파일 처리 실패)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value =
                                    "{\"status\": 500, \"errorType\": \"ServerError\", \"message\": \"서버 처리 중 오류가 발생했습니다.\"}")
                    )
            )
    })
    @PostMapping(value = "/analyze-mood",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FlowerMoodResponseDTO> analyzeMood(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @RequestParam("voice") MultipartFile voiceFile) {

        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        // 파일 기본 검증
        if (voiceFile == null || voiceFile.isEmpty()) {
            throw new InvalidInputException(ErrorCode.INVALID_VOICE_MESSAGE);
        }

        // 파일 타입 검증
        String contentType = voiceFile.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new InvalidInputException(ErrorCode.INVALID_VOICE_MESSAGE);
        }

        log.info("음성 분석 요청 시작 - 사용자 ID: {}, 파일크기: {}, 파일타입: {}",
                loginInfo.getUserId(),
                voiceFile.getSize(),
                contentType);

        FlowerMoodResponseDTO response = flowerService.analyzeMood(loginInfo.getUserId(), voiceFile);

        log.info("음성 분석 완료 - 사용자 ID: {}, 기분 상태: {}, 파일 저장: {}",
                loginInfo.getUserId(),
                response.getMood(),
                "긍정".equals(response.getMood()) || "중립".equals(response.getMood()));

        return ResponseEntity.ok(response);
    }


    /**
     * 꽃 이름 설정 및 수정
     *
     */
    @Operation(summary = "꽃 이름 설정 및 수정", description = "사용자의 꽃에 이름을 설정하거나 기존 이름을 수정합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "이름 설정 또는 수정 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청입니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 400, \"errorType\": \"BadRequest\", \"message\": \"유효하지 않은 요청입니다.\"}")
                    )
            )
    })
    @PostMapping("/set-name")
    public ResponseEntity<Void> setFlowerName(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @RequestBody FlowerRequestDTO request) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("꽃 이름 설정 요청 - 사용자 ID: {}, 새로운 이름: {}", loginInfo.getUserId(), request.getName());
        flowerService.setFlowerName(loginInfo.getUserId(), request.getName());
        log.info("꽃 이름 설정 성공 - 새로운 이름: {}", request.getName());
        return ResponseEntity.ok().build();
    }


    /**
     * 새로운 씨앗 키우기
     *
     */
    @Operation(summary = "새로운 씨앗 키우기", description = "기분 상태 카운트를 초기화하고 새로운 꽃을 키우기 시작합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "씨앗 초기화 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청입니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 400, \"errorType\": \"BadRequest\", \"message\": \"유효하지 않은 요청입니다.\"}")
                    )
            )
    })
    @PostMapping("/new-seed")
    public ResponseEntity<Void> startNewSeed(@AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("새로운 씨앗 시작 요청 - 사용자 ID: {}", loginInfo.getUserId());
        flowerService.startNewSeed(loginInfo.getUserId());
        log.info("새로운 씨앗 시작 성공 - 사용자 ID: {}", loginInfo.getUserId());
        return ResponseEntity.ok().build();
    }

    /**
     * 음성 메시지 청취 API
     */
    @Operation(summary = "음성 메시지 청취", description = "저장된 음성 메시지를 청취하고 청취 완료 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "음성 메시지 URL 반환 성공"),
            @ApiResponse(responseCode = "404", description = "음성 메시지를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    @GetMapping("/voice/{flowerId}")
    public ResponseEntity<String> listenVoiceMessage(
            @AuthenticationPrincipal LoginInfo loginInfo) {

        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("음성 메시지 청취 요청 - 사용자 ID: {}, 꽃 ID: {}", loginInfo.getUserId());

        String voiceUrl = flowerService.getPartnerVoiceMessage(loginInfo.getUserId());

        return ResponseEntity.ok(voiceUrl);
    }

    /**
     * 음성 메시지 조회
     */
    @Operation(summary = "음성 메시지 상태 조회", description = "음성 메시지의 녹음 완료 및 청취 완료 상태를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 조회 성공"),
            @ApiResponse(responseCode = "404", description = "꽃을 찾을 수 없음")
    })
    @GetMapping("/voice/status/{flowerId}")
    public ResponseEntity<VoiceMessageStatusDTO> getVoiceMessageStatus(
            @AuthenticationPrincipal LoginInfo loginInfo) {

        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("음성 메시지 상태 조회 요청 - 사용자 ID: {}", loginInfo.getUserId());

        VoiceMessageStatusDTO status = flowerService.getCoupleVoiceMessageStatus(loginInfo.getUserId());

        return ResponseEntity.ok(status);
    }
}
