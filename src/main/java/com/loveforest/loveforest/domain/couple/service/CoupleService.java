package com.loveforest.loveforest.domain.couple.service;

import com.loveforest.loveforest.domain.couple.dto.CoupleCodeResponseDTO;
import com.loveforest.loveforest.domain.couple.dto.CoupleResponseDTO;
import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.exception.CoupleCodeAlreadyUsedException;
import com.loveforest.loveforest.domain.couple.exception.CoupleNotFoundException;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.daily_mission.repository.DailyMissionRepository;
import com.loveforest.loveforest.domain.flower.service.FlowerService;
import com.loveforest.loveforest.domain.pet.service.PetService;
import com.loveforest.loveforest.domain.room.service.RoomServiceImpl;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.exception.UserNotFoundException;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CoupleService {

    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private final PetService petService; // PetService 추가
    private final RoomServiceImpl roomServiceImpl;
    private final FlowerService flowerService;
    private final DailyMissionRepository dailyMissionRepository;


    public String generateCoupleCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    /**
     * 커플 코드로 두 번째 사용자를 커플에 연동
     * */
    @Transactional
    public void joinCouple(Long userId, String coupleCode) {
        // 1. 요청한 사용자 조회
        User requestUser = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 사용자가 이미 속한 커플이 있을 경우
        Couple currentCouple = requestUser.getCouple();
        if (currentCouple != null) {
            if (currentCouple.getUsers().size() < 2) {
                dailyMissionRepository.deleteByCouple(currentCouple);
                // 기존 Couple의 모든 User들의 couple 속성을 null로 설정
                for (User existingUser : currentCouple.getUsers()) {
                    existingUser.setCouple(null);
                }
                userRepository.saveAll(currentCouple.getUsers());
                coupleRepository.delete(currentCouple);
            } else {
                throw new IllegalStateException("이미 다른 커플과 연동된 상태입니다.");
            }
        }

        // 3. 타겟 커플 조회
        Couple targetCouple = coupleRepository.findByCoupleCode(coupleCode)
                .orElseThrow(CoupleNotFoundException::new);

        // 4. 타겟 커플이 이미 두 명인지 확인
        if (targetCouple.getUsers().size() >= 2) {
            throw new CoupleCodeAlreadyUsedException();
        }

        // 5. 기념일 동기화 및 커플 연동
        LocalDate anniversaryDate = targetCouple.getUsers().get(0).getAnniversaryDate();
        requestUser.setAnniversaryDate(anniversaryDate);
        requestUser.setCouple(targetCouple);
        targetCouple.addUser(requestUser);
        userRepository.save(requestUser);

        // 6. 커플에 대한 꽃 생성
        if (targetCouple.getUsers().size() == 2) {
            // 각 사용자에 대해 꽃 생성
            for (User member : targetCouple.getUsers()) {
                flowerService.createFlowerForUser(member);
            }

            // Pet & Room 생성도 함께 진행
            petService.createPetForCouple(targetCouple);
            roomServiceImpl.createRoom(targetCouple);
        }

        coupleRepository.save(targetCouple);
        log.info("커플 연동 프로세스 완료 - 커플 ID: {}", targetCouple.getId());
    }




    public CoupleCodeResponseDTO getMyCoupleCode(Long userId) {
        // 요청한 사용자
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 커플 정보가 있는지 확인
        Couple couple = user.getCouple();
        if (couple == null) {
            throw new CoupleNotFoundException(); // 커플이 없을 경우 예외 처리
        }

        // 커플 코드 반환
        return new CoupleCodeResponseDTO(couple.getCoupleCode());
    }

    /**
     * 사용자 ID로 커플 조회
     *
     * @param userId 사용자 ID
     * @return 사용자와 연동된 커플 객체 반환
     */
    @Transactional(readOnly = true)
    public Couple getCoupleByUserId(Long userId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 커플 정보 확인
        Couple couple = user.getCouple();
        if (couple == null) {
            throw new CoupleNotFoundException();
        }
        return couple;
    }

    /**
     * 커플 ID로 커플 정보 조회
     *
     * @param coupleId 커플 ID
     * @return 사용자와 연동된 커플 객체 반환
     */
    public CoupleResponseDTO getCoupleInfo(Long coupleId) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(CoupleNotFoundException::new);

        // 첫 번째 사용자의 기념일을 가져옴
        String anniversaryDate = couple.getUsers().isEmpty() ? null :
                couple.getUsers().get(0).getAnniversaryDate().toString();

        return CoupleResponseDTO.builder()
                .coupleId(couple.getId())
                .coupleCode(couple.getCoupleCode())
                .points(couple.getPoints())
                .anniversaryDate(anniversaryDate)
                .build();
    }

    @Transactional
    public void addPointsToCouple(Long coupleId, int points) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(CoupleNotFoundException::new);

        couple.addPoints(points);
        coupleRepository.save(couple);

        log.info("커플 포인트 추가 - 커플 ID: {}, 추가된 포인트: {}, 현재 포인트: {}", coupleId, points, couple.getPoints());
    }
}