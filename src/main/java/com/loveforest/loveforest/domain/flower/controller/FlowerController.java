package com.loveforest.loveforest.domain.flower.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.flower.dto.FlowerMoodResponseDTO;
import com.loveforest.loveforest.domain.flower.dto.FlowerRequestDTO;
import com.loveforest.loveforest.domain.flower.service.FlowerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/flower")
@RequiredArgsConstructor
@Slf4j
public class FlowerController {

    private final FlowerService flowerService;

    @Operation(summary = "꽃의 기분 상태 분석", description = "사용자의 음성 메시지를 분석하여 기분 상태를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "기분 상태 분석 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    @PostMapping("/analyze-mood")
    public ResponseEntity<FlowerMoodResponseDTO> analyzeMood(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @RequestBody byte[] voiceMessage) {

        log.info("음성 분석 요청 시작 - 사용자 ID: {}", loginInfo.getUserId());
        FlowerMoodResponseDTO response = flowerService.analyzeMood(loginInfo.getUserId(), voiceMessage);
        log.info("음성 분석 성공 - 기분 상태: {}", response.getMood());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "꽃 이름 설정 및 수정", description = "꽃의 이름을 설정하거나 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이름 설정 또는 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    @PostMapping("/set-name")
    public ResponseEntity<Void> setFlowerName(
            @AuthenticationPrincipal LoginInfo loginInfo,
            @RequestBody FlowerRequestDTO request) {

        log.info("꽃 이름 설정 요청 - 사용자 ID: {}, 새로운 이름: {}", loginInfo.getUserId(), request.getName());
        flowerService.setFlowerName(loginInfo.getUserId(), request.getName());
        log.info("꽃 이름 설정 성공 - 새로운 이름: {}", request.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "새로운 씨앗 키우기", description = "기분 상태 카운트를 초기화하고 새로운 꽃을 시작합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "씨앗 초기화 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    @PostMapping("/new-seed")
    public ResponseEntity<Void> startNewSeed(@AuthenticationPrincipal LoginInfo loginInfo) {
        log.info("새로운 씨앗 시작 요청 - 사용자 ID: {}", loginInfo.getUserId());
        flowerService.startNewSeed(loginInfo.getUserId());
        log.info("새로운 씨앗 시작 성공 - 사용자 ID: {}", loginInfo.getUserId());
        return ResponseEntity.ok().build();
    }
}
