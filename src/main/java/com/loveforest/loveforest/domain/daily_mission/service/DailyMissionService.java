package com.loveforest.loveforest.domain.daily_mission.service;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.exception.CoupleNotFoundException;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.daily_mission.dto.DailyMissionResponseDTO;
import com.loveforest.loveforest.domain.daily_mission.dto.WeeklyMissionResponseDTO;
import com.loveforest.loveforest.domain.daily_mission.entity.DailyMission;
import com.loveforest.loveforest.domain.daily_mission.exception.MissionAlreadyAnsweredException;
import com.loveforest.loveforest.domain.daily_mission.exception.MissionNotFoundException;
import com.loveforest.loveforest.domain.daily_mission.exception.PreviousMissionIncompleteException;
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

    @Value("${ai.server.url}")
    private String aiServerUrl;

    // 매주 월요일 새벽 1시에 일주일치 미션 생성
    @Scheduled(cron = "0 0 1 * * MON")
    @Transactional
    public void generateWeeklyMissions() {
        log.info("주간 미션 생성 시작");
        LocalDate today = LocalDate.now();

        try {
            if (dailyMissionRepository.existsByMissionDate(today)) {
                log.info("이미 금주의 미션이 생성되어 있음");
                return;
            }

            List<WeeklyMissionResponseDTO.DailyMissionContent> weeklyMissions = requestWeeklyMissionsFromAI();
            log.info("AI 서버로부터 {} 개의 미션 수신", weeklyMissions.size());

            // 모든 커플에 대해 미션 생성
            List<Couple> couples = coupleRepository.findAll();
            for (Couple couple : couples) {
                createWeeklyMissionsForCouple(couple, weeklyMissions);
            }

            log.info("주간 미션 생성 완료");
        } catch (Exception e) {
            log.error("주간 미션 생성 중 오류 발생", e);
            throw new RuntimeException("주간 미션 생성 실패", e);
        }
    }

    private void createWeeklyMissionsForCouple(Couple couple, List<WeeklyMissionResponseDTO.DailyMissionContent> weeklyMissions) {
        for (int i = 0; i < weeklyMissions.size(); i++) {
            WeeklyMissionResponseDTO.DailyMissionContent missionContent = weeklyMissions.get(i);
            createDailyMission(couple, missionContent, i + 1);
        }
    }

    private void createDailyMission(Couple couple, WeeklyMissionResponseDTO.DailyMissionContent missionContent, int missionNumber) {
        DailyMission mission = new DailyMission(
                missionNumber,
                missionContent.getDate(),
                missionContent.getContent(),
                couple
        );
        dailyMissionRepository.save(mission);
        log.debug("미션 생성 완료 - 커플ID: {}, 날짜: {}, 미션번호: {}",
                couple.getId(), missionContent.getDate(), missionNumber);
    }

    // AI 서버에서 일주일치 미션 받아오기
    private List<WeeklyMissionResponseDTO.DailyMissionContent> requestWeeklyMissionsFromAI() {
        try {
            ResponseEntity<WeeklyMissionResponseDTO> response = restTemplate.exchange(
                    aiServerUrl + "/generate_question",
                    HttpMethod.GET,
                    null,
                    WeeklyMissionResponseDTO.class
            );

            if (response.getBody() == null) {
                log.error("AI 서버로부터 받은 응답이 null입니다.");
                throw new RuntimeException("Invalid response from AI server");
            }

            return response.getBody().getMissions();
        } catch (RestClientException e) {
            log.error("AI 서버 호출 중 오류 발생", e);
            throw new RuntimeException("AI server communication failed", e);
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
        LocalDate today = LocalDate.now();

        DailyMission mission = dailyMissionRepository.findByCouple_IdAndMissionDate(coupleId, today)
                .orElseThrow(() -> {
                    log.warn("미션을 찾을 수 없음 - 커플 ID: {}", coupleId);
                    return new MissionNotFoundException();
                });

        // 커플의 첫 번째 사용자인지 확인
        boolean isPartner1 = isFirstPartner(coupleId, userId);

        // 이미 답변했는지 확인
        if (isAlreadyAnswered(mission, isPartner1)) {
            log.warn("이미 답변한 미션 - 커플 ID: {}, 사용자 ID: {}", coupleId, userId);
            throw new MissionAlreadyAnsweredException();
        }

        mission.updateAnswer(mood, answer, isPartner1);
        log.info("미션 답변 저장 완료 - 커플 ID: {}", coupleId);
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
                !mission.getPartner1Answer().equals("null") :
                !mission.getPartner2Answer().equals("null");
    }

    // 미션 기록 조회
    @Transactional(readOnly = true)
    public List<DailyMissionResponseDTO> getMissionHistory(Long coupleId) {
        return dailyMissionRepository.findByCouple_IdOrderByMissionDateDesc(coupleId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DailyMissionResponseDTO convertToDTO(DailyMission mission) {
        return new DailyMissionResponseDTO(
                mission.getMissionNumber(),
                mission.getMissionDate(),
                mission.getMissionContent(),
                mission.getPartner1Mood(),
                mission.getPartner1Answer(),
                mission.getPartner2Mood(),
                mission.getPartner2Answer(),
                mission.isCompleted()
        );
    }
}