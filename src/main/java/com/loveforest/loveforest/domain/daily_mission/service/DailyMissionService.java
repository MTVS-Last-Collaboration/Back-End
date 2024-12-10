package com.loveforest.loveforest.domain.daily_mission.service;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.exception.CoupleNotFoundException;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.daily_mission.dto.DailyMissionResponseDTO;
import com.loveforest.loveforest.domain.daily_mission.dto.WeeklyMissionRequestDTO;
import com.loveforest.loveforest.domain.daily_mission.dto.WeeklyMissionResponseDTO;
import com.loveforest.loveforest.domain.daily_mission.entity.DailyMission;
import com.loveforest.loveforest.domain.daily_mission.exception.*;
import com.loveforest.loveforest.domain.daily_mission.repository.DailyMissionRepository;
import com.loveforest.loveforest.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
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
    public void generateWeeklyMissionsManually(Long coupleId) {
        List<WeeklyMissionResponseDTO.DailyMissionContent> weeklyMissions = getMissionsFromAI(coupleId); // 트랜잭션 외부
        validateAndGenerateMissions(weeklyMissions);
    }

    /**
     * 미션 생성의 공통 로직
     * DRY 원칙을 적용하여 중복 코드 제거
     */
    private void validateAndGenerateMissions(List<WeeklyMissionResponseDTO.DailyMissionContent> weeklyMissions) {

        try {
            validateMissions(weeklyMissions); // 미션 데이터 검증 로직 분리

            createMissionsForAllCouples(weeklyMissions);
            log.info("주간 미션 생성 완료");
        } catch (AIServerException | MissionGenerationException e) {
            log.error("주간 미션 생성 실패: {}", e.getMessage());
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
    private List<WeeklyMissionResponseDTO.DailyMissionContent> getMissionsFromAI(Long coupleId) {
        try {
            // HTTP 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 요청 DTO 생성
            WeeklyMissionRequestDTO requestDTO = new WeeklyMissionRequestDTO(
                    coupleId
            );

            // HTTP 엔티티 생성
            HttpEntity<WeeklyMissionRequestDTO> requestEntity =
                    new HttpEntity<>(requestDTO, headers);

            ResponseEntity<WeeklyMissionResponseDTO> response = restTemplate.exchange(
                    aiServerUrl + AI_ENDPOINT,
                    HttpMethod.POST,  // POST로 변경
                    requestEntity,
                    WeeklyMissionResponseDTO.class
            );

            if (response.getBody() == null || response.getBody().getMissions() == null) {
                log.error("AI 서버로부터 유효하지 않은 응답 수신");
                throw new AIServerException();
            }

            // 응답 데이터 유효성 검사
            validateMissionResponse(response.getBody().getMissions());

            return response.getBody().getMissions();
        } catch (RestClientException e) {
            log.error("AI 서버 통신 중 오류 발생: {}", e.getMessage());
            throw new AIServerException();
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage());
            throw new AIServerException();
        }
    }

    /**
     *응답 데이터 유효성 검사
     */
    private void validateMissionResponse(List<WeeklyMissionResponseDTO.DailyMissionContent> missions) {
        if (missions.size() != 7) {
            log.error("잘못된 미션 개수: {}", missions.size());
            throw new AIServerException();
        }

        // 각 미션의 유효성 검사
        missions.forEach(mission -> {
            if (mission.getDate() == null ||
                    mission.getContent() == null ||
                    mission.getContent().trim().isEmpty()) {
                log.error("유효하지 않은 미션 데이터 발견");
                throw new AIServerException();
            }
        });
    }

    /**
     * 모든 커플에 대해 미션을 생성하는 메서드
     * LSP(리스코프 치환 원칙)를 준수하여 커플별 미션 생성 로직을 분리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createMissionsForAllCouples(List<WeeklyMissionResponseDTO.DailyMissionContent> missions) {
        List<Couple> couples = coupleRepository.findAll();

        for (Couple couple : couples) {
            try {
                createMissionsForCouple(couple, missions);
                log.debug("커플 ID: {}에 대한 미션 생성 완료", couple.getId());
            } catch (Exception e) {
                log.error("커플 ID: {}에 대한 미션 생성 실패", couple.getId(), e);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                throw new MissionGenerationException();
            }
        }
    }


    /**
     * 특정 커플에 대한 미션을 생성하는 메서드
     * ISP(인터페이스 분리 원칙)를 준수하여 미션 생성의 세부 로직을 분리
     */
    @Transactional
    private void createMissionsForCouple(Couple couple, List<WeeklyMissionResponseDTO.DailyMissionContent> missions) {
        for (WeeklyMissionResponseDTO.DailyMissionContent mission : missions) {
            // 중복 데이터 확인
            boolean exists = dailyMissionRepository.existsByCoupleIdAndMissionDate(couple.getId(), mission.getDate());
            if (exists) {
                log.warn("이미 존재하는 미션입니다: 커플 ID: {}, 날짜: {}", couple.getId(), mission.getDate());
                continue; // 중복된 데이터는 건너뜁니다
            }

            // 중복되지 않은 경우에만 저장
            DailyMission dailyMission = new DailyMission(
                    mission.getDate(),
                    mission.getContent(),
                    couple
            );
            dailyMissionRepository.save(dailyMission);
        }
    }


    @Transactional(readOnly = true)
    public DailyMissionResponseDTO getCurrentMission(Long coupleId) {
        log.debug("현재 미션 조회 - 커플 ID: {}", coupleId);
        LocalDate today = LocalDate.now();

        // 미션 존재 여부 먼저 확인
        if (!dailyMissionRepository.existsByCouple_IdAndMissionDate(coupleId, today)) {
            throw new MissionNotFoundException();
        }

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
        log.debug("미션 답변 저장/수정 시작 - 커플 ID: {}, 사용자 ID: {}", coupleId, userId);

        DailyMission mission = getCurrentDailyMission(coupleId);

        // 미션이 완료된 경우 수정 불가
//        if (mission.isCompleted()) {
//            log.warn("이미 완료된 미션은 수정할 수 없음 - 커플 ID: {}, 미션 ID: {}", coupleId, mission.getId());
//            throw new MissionAlreadyCompletedException();
//        }

        boolean isPartner1 = isFirstPartner(coupleId, userId);

        // 자신의 이전 답변 로깅
        if (isPartner1) {
            log.debug("파트너1의 이전 답변 - mood: {}, answer: {}", mission.getPartner1Mood(), mission.getPartner1Answer());
        } else {
            log.debug("파트너2의 이전 답변 - mood: {}, answer: {}", mission.getPartner2Mood(), mission.getPartner2Answer());
        }

        mission.updateAnswer(mood, answer, isPartner1);

        // 변경된 미션 저장
        DailyMission savedMission = dailyMissionRepository.save(mission);

        // 저장된 결과 로깅
        log.info("미션 답변 저장 완료 - 커플 ID: {}, 파트너1답변: {}, 파트너2답변: {}",
                coupleId,
                savedMission.getPartner1Answer(),
                savedMission.getPartner2Answer());

        if (savedMission.isCompleted()) {
            log.info("미션 완료됨 - 포인트 지급 완료");
        }
    }

    private DailyMission getCurrentDailyMission(Long coupleId) {
        return dailyMissionRepository.findByCouple_IdAndMissionDate(coupleId, LocalDate.now())
                .orElseThrow(() -> {
                    log.warn("오늘의 미션을 찾을 수 없음 - 커플 ID: {}", coupleId);
                    return new MissionNotFoundException();
                });
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
        List<User> users = mission.getCouple().getUsers();
        // 커플의 두 사용자 정보 가져오기
        String partner1Name = !users.isEmpty() ? users.get(0).getNickname() : "Unknown";
        String partner2Name = users.size() > 1 ? users.get(1).getNickname() : "Unknown";
        return DailyMissionResponseDTO.builder()
                .missionNumber(mission.getMissionNumber())
                .missionDate(mission.getMissionDate())
                .missionContent(mission.getMissionContent())
                .partner1Name(partner1Name)
                .partner1Mood(mission.getPartner1Mood())
                .partner1Answer(mission.getPartner1Answer())
                .partner2Name(partner2Name)
                .partner2Mood(mission.getPartner2Mood())
                .partner2Answer(mission.getPartner2Answer())
                .isCompleted(mission.isCompleted())
                .build();
    }
}