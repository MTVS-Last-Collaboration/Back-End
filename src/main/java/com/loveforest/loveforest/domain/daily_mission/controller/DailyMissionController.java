package com.loveforest.loveforest.domain.daily_mission.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.daily_mission.dto.DailyMissionResponseDTO;
import com.loveforest.loveforest.domain.daily_mission.service.DailyMissionService;
import com.loveforest.loveforest.domain.user.exception.LoginRequiredException;
import com.loveforest.loveforest.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Tag(name = "데일리 미션 API", description = "커플 간의 1일 1문답 미션 관련 API")
public class DailyMissionController {
    private final DailyMissionService dailyMissionService;

    @Operation(
            summary = "수동으로 일주일치 미션 생성",
            description = "AI 서버에서 일주일치 미션을 받아와 생성합니다."
    )
    @PostMapping("/generate-weekly")
    public ResponseEntity<String> generateWeeklyMissions(@AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }

        log.info("일주일치 미션 수동 생성 요청 - 요청자 ID: {}", loginInfo.getUserId());
        dailyMissionService.generateWeeklyMissionsManually();
        return ResponseEntity.ok("일주일치 미션이 성공적으로 생성되었습니다.");
    }

    @Operation(
            summary = "현재 미션 조회", description = "오늘의 미션을 조회합니다. 이전 미션이 완료되지 않은 경우 이전 미션이 반환됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "미션 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DailyMissionResponseDTO.class),
                                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            value = """
                                            {
                                              "missionNumber": 1,
                                              "missionDate": "2024-11-08",
                                              "missionContent": "오늘 하루 동안 가장 행복했던 순간은?",
                                              "partner1Mood": "행복",
                                              "partner1Answer": "함께 산책할 때입니다.",
                                              "partner2Mood": "기쁨",
                                              "partner2Answer": "맛있는 저녁을 먹었을 때입니다.",
                                              "completed": true
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "미션을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            value = """
                                            {
                                              "timestamp": "2024-11-08T10:00:00",
                                              "status": 404,
                                              "errorType": "Mission Not Found",
                                              "message": "해당 미션을 찾을 수 없습니다.",
                                              "code": "MISSION-001"
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/current")
    public ResponseEntity<DailyMissionResponseDTO> getCurrentMission(
            @Parameter(hidden = true) @AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }
        log.info("현재 미션 조회 요청 - 커플 ID: {}", loginInfo.getCoupleId());
        DailyMissionResponseDTO mission = dailyMissionService.getCurrentMission(loginInfo.getCoupleId());
        log.info("미션 조회 완료 - 미션 번호: {}, 날짜: {}", mission.getMissionNumber(), mission.getMissionDate());
        return ResponseEntity.ok(mission);
    }

    @Operation(summary = "미션 답변 저장", description = "현재 미션에 대한 답변을 저장합니다. 이미 답변한 경우 예외가 발생합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "답변 저장 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            value = "\"답변이 저장되었습니다.\""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "이미 답변한 미션",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            value = """
                                            {
                                              "timestamp": "2024-11-08T10:00:00",
                                              "status": 409,
                                              "errorType": "Mission Already Answered",
                                              "message": "이미 답변한 미션입니다.",
                                              "code": "MISSION-003"
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/answer")
    public ResponseEntity<String> saveAnswer(
            @Parameter(hidden = true) @AuthenticationPrincipal LoginInfo loginInfo,
            @Parameter(description = "사용자의 현재 기분", required = true) @RequestParam String mood,
            @Parameter(description = "미션에 대한 답변", required = true) @RequestParam String answer) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }
        log.info("미션 답변 저장 요청 - 커플 ID: {}, 사용자 ID: {}", loginInfo.getCoupleId(), loginInfo.getUserId());
        dailyMissionService.saveAnswer(loginInfo.getCoupleId(), loginInfo.getUserId(), mood, answer);
        log.info("미션 답변 저장 완료");
        return ResponseEntity.ok("답변이 저장되었습니다.");
    }

    @Operation(summary = "미션 히스토리 조회", description = "모든 이전 미션 기록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "미션 히스토리 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = DailyMissionResponseDTO.class),
                                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            value = """
                                            [
                                              {
                                                "missionNumber": 2,
                                                "missionDate": "2024-11-08",
                                                "missionContent": "서로에게 가장 고마웠던 순간은?",
                                                "partner1Mood": "감동",
                                                "partner1Answer": "아플 때 병원에 데려다줘서 고마웠어요.",
                                                "partner2Mood": "행복",
                                                "partner2Answer": "힘들 때 응원해줘서 고마웠어요.",
                                                "completed": true
                                              },
                                              {
                                                "missionNumber": 1,
                                                "missionDate": "2024-11-07",
                                                "missionContent": "처음 만났을 때 기억나는 점은?",
                                                "partner1Mood": "설렘",
                                                "partner1Answer": "첫 인상이 정말 좋았어요.",
                                                "partner2Mood": "기쁨",
                                                "partner2Answer": "웃는 모습이 예뻤어요.",
                                                "completed": true
                                              }
                                            ]
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/history")
    public ResponseEntity<List<DailyMissionResponseDTO>> getMissionHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal LoginInfo loginInfo) {
        if (loginInfo == null) {
            throw new LoginRequiredException();
        }
        log.info("미션 히스토리 조회 요청 - 커플 ID: {}", loginInfo.getCoupleId());
        List<DailyMissionResponseDTO> history = dailyMissionService.getMissionHistory(loginInfo.getCoupleId());
        log.info("미션 히스토리 조회 완료 - 조회된 미션 수: {}", history.size());
        return ResponseEntity.ok(history);
    }
}
