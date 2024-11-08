package com.loveforest.loveforest.domain.daily_mission.service;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.exception.CoupleNotFoundException;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.daily_mission.dto.DailyMissionResponseDTO;
import com.loveforest.loveforest.domain.daily_mission.dto.WeeklyMissionResponseDTO;
import com.loveforest.loveforest.domain.daily_mission.entity.DailyMission;
import com.loveforest.loveforest.domain.daily_mission.exception.*;
import com.loveforest.loveforest.domain.daily_mission.repository.DailyMissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyMissionService {

    private final DailyMissionRepository dailyMissionRepository;
    private final RestTemplate restTemplate;
    private final CoupleRepository coupleRepository;
    private static final String DEFAULT_ANSWER = "null";
    private static final String AI_ENDPOINT = "/generate_question";

    @Value("${ai.server.url}")
    private String aiServerUrl;

    /**
     * 수동으로 일주일치 미션을 생성하는 메서드
     */
    @Transactional
    public void generateWeeklyMissionsManually() {
        validateAndGenerateMissions(LocalDate.now());
    }

    /**
     * 스케줄링된 주간 미션 생성
     */
    @Scheduled(cron = "0 0 1 * * MON")
    @Transactional
    public void generateWeeklyMissions() {
        validateAndGenerateMissions(LocalDate.now());
    }

    /**
     * 미션 생성의 공통 로직
     * DRY 원칙을 적용하여 중복 코드 제거
     */
    private void validateAndGenerateMissions(LocalDate date) {
        log.info("주간 미션 생성 시작 - 날짜: {}", date);

        validateMissionDate(date); // 날짜 검증 로직 분리

        try {
            List<WeeklyMissionResponseDTO.DailyMissionContent> weeklyMissions = getMissionsFromAI();
            validateMissions(weeklyMissions); // 미션 데이터 검증 로직 분리

            createMissionsForAllCouples(weeklyMissions);
            log.info("주간 미션 생성 완료");
        } catch (AIServerException | MissionGenerationException e) {
            throw e; // 이미 처리된 예외는 그대로 전파
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생", e);
            throw new MissionGenerationException();
        }
    }

    private void validateMissionDate(LocalDate date) {
        if (dailyMissionRepository.existsByMissionDate(date)) {
            log.warn("이미 해당 주의 미션이 존재함 - 날짜: {}", date);
            throw new DailyMissionAlreadyExistsException();
        }
    }

    private void validateMissions(List<WeeklyMissionResponseDTO.DailyMissionContent> missions) {
        if (missions == null || missions.isEmpty()) {
            throw new MissionGenerationException();
        }
    }

    /**
     * AI 서버에서 미션을 받아오는 메서드
     */
    private List<WeeklyMissionResponseDTO.DailyMissionContent> getMissionsFromAI() {
        try {
            ResponseEntity<WeeklyMissionResponseDTO> response = restTemplate.exchange(
                    aiServerUrl + AI_ENDPOINT,
                    HttpMethod.GET,
                    null,
                    WeeklyMissionResponseDTO.class
            );

            if (response.getBody() == null || response.getBody().getMissions() == null) {
                throw new AIServerException();
            }

            return response.getBody().getMissions();
        } catch (RestClientException e) {
            log.error("AI 서버 통신 중 오류 발생", e);
            throw new AIServerException();
        }
    }

    /**
     * 모든 커플에 대해 미션을 생성하는 메서드
     * LSP(리스코프 치환 원칙)를 준수하여 커플별 미션 생성 로직을 분리
     */
    private void createMissionsForAllCouples(List<WeeklyMissionResponseDTO.DailyMissionContent> missions) {
        List<Couple> couples = coupleRepository.findAll();

        for (Couple couple : couples) {
            try {
                createMissionsForCouple(couple, missions);
                log.debug("커플 ID: {}에 대한 미션 생성 완료", couple.getId());
            } catch (Exception e) {
                log.error("커플 ID: {}에 대한 미션 생성 실패", couple.getId(), e);
            }
        }
    }


    /**
     * 특정 커플에 대한 미션을 생성하는 메서드
     * ISP(인터페이스 분리 원칙)를 준수하여 미션 생성의 세부 로직을 분리
     */
    private void createMissionsForCouple(Couple couple, List<WeeklyMissionResponseDTO.DailyMissionContent> missions) {
        for (int i = 0; i < missions.size(); i++) {
            WeeklyMissionResponseDTO.DailyMissionContent mission = missions.get(i);
            DailyMission dailyMission = new DailyMission(
                    i + 1,
                    mission.getDate(),
                    mission.getContent(),
                    couple
            );
            dailyMissionRepository.save(dailyMission);
            log.debug("미션 생성 완료 - 커플ID: {}, 날짜: {}, 미션번호: {}",
                    couple.getId(), mission.getDate(), i + 1);
        }
    }


    @Transactional(readOnly = true)
    public DailyMissionResponseDTO getCurrentMission(Long coupleId) {
        log.debug("현재 미션 조회 - 커플 ID: {}", coupleId);
        LocalDate today = LocalDate.now();

        DailyMission mission = dailyMissionRepository.findByCouple_IdAndMissionDate(coupleId, today)
                .orElseThrow(() -> {
                    log.warn("미션을 찾을 수 없음 - 커플 ID: {}, 날짜: {}", coupleId, today);
                    return new MissionNotFoundException();
                });

        return convertToDTO(mission);
    }

    // 답변 저장
    @Transactional
    public void saveAnswer(Long coupleId, Long userId, String mood, String answer) {
        log.debug("미션 답변 저장 시작 - 커플 ID: {}, 사용자 ID: {}", coupleId, userId);

        DailyMission mission = getCurrentDailyMission(coupleId);
        validateAnswerSubmission(mission, coupleId, userId);

        boolean isPartner1 = isFirstPartner(coupleId, userId);
        mission.updateAnswer(mood, answer, isPartner1);

        log.info("미션 답변 저장 완료 - 커플 ID: {}", coupleId);
    }

    private DailyMission getCurrentDailyMission(Long coupleId) {
        return dailyMissionRepository.findByCouple_IdAndMissionDate(coupleId, LocalDate.now())
                .orElseThrow(() -> {
                    log.warn("오늘의 미션을 찾을 수 없음 - 커플 ID: {}", coupleId);
                    return new MissionNotFoundException();
                });
    }

    private void validateAnswerSubmission(DailyMission mission, Long coupleId, Long userId) {
        boolean isPartner1 = isFirstPartner(coupleId, userId);
        if (isAlreadyAnswered(mission, isPartner1)) {
            log.warn("이미 답변한 미션 - 커플 ID: {}, 사용자 ID: {}", coupleId, userId);
            throw new MissionAlreadyAnsweredException();
        }
    }

    // 첫 번째 파트너인지 확인하는 메서드
    private boolean isFirstPartner(Long coupleId, Long userId) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(CoupleNotFoundException::new);

        // 커플의 users 리스트에서 첫 번째 사용자의 ID와 비교
        return couple.getUsers().get(0).getId().equals(userId);
    }

    private boolean isAlreadyAnswered(DailyMission mission, boolean isPartner1) {
        return isPartner1 ?
                !mission.getPartner1Answer().equals(DEFAULT_ANSWER) :
                !mission.getPartner2Answer().equals(DEFAULT_ANSWER);
    }


    // 미션 기록 조회
    @Transactional(readOnly = true)
    public List<DailyMissionResponseDTO> getMissionHistory(Long coupleId) {
        return dailyMissionRepository.findByCouple_IdOrderByMissionDateDesc(coupleId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DailyMissionResponseDTO convertToDTO(DailyMission mission) {
        return DailyMissionResponseDTO.builder()
                .missionNumber(mission.getMissionNumber())
                .missionDate(mission.getMissionDate())
                .missionContent(mission.getMissionContent())
                .partner1Mood(mission.getPartner1Mood())
                .partner1Answer(mission.getPartner1Answer())
                .partner2Mood(mission.getPartner2Mood())
                .partner2Answer(mission.getPartner2Answer())
                .isCompleted(mission.isCompleted())
                .build();
    }
}