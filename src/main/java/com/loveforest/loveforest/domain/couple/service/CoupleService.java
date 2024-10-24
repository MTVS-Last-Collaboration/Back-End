package com.loveforest.loveforest.domain.couple.service;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.exception.CoupleCodeAlreadyUsedException;
import com.loveforest.loveforest.domain.couple.exception.CoupleNotFoundException;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.room.entity.Room;
import com.loveforest.loveforest.domain.room.repository.RoomRepository;
import com.loveforest.loveforest.domain.user.dto.UserSignupRequestDTO;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.exception.UserNotFoundException;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class CoupleService {

    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;


    // 커플 코드 생성 및 발급
    public String createCouple(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 1. 커플이 성사된 상태인지 확인
        if (user.getCouple() != null) {
            throw new IllegalStateException("이미 커플이 성사된 상태입니다. 커플 코드를 다시 생성할 수 없습니다.");
        }

        // 2. 이미 사용자가 커플 코드를 가지고 있는지 확인
        Couple existingCouple = coupleRepository.findByUsersContaining(user);
        if (existingCouple != null) {
            // 기존 커플 코드 삭제
            coupleRepository.delete(existingCouple);
        }

        // 3. 새로운 커플 코드 생성
        String coupleCode = UUID.randomUUID().toString();
        Couple newCouple = new Couple(coupleCode);
        newCouple.addUser(user);
        coupleRepository.save(newCouple);

        return coupleCode;  // 생성된 커플 코드 반환
    }

    // 커플 코드로 두 번째 사용자를 커플에 연동
    public void joinCouple(Long userId, String coupleCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 이미 사용자가 다른 커플과 연동되어 있는지 확인
        Couple existingCouple = coupleRepository.findByUsersContaining(user);
        if (existingCouple != null) {
            throw new IllegalStateException("이미 다른 커플과 연동된 상태입니다. 새로운 커플과 연동할 수 없습니다.");
        }

        Couple couple = coupleRepository.findByCoupleCode(coupleCode)
                .orElseThrow(CoupleNotFoundException::new);

        // 이미 두 명의 사용자가 커플에 연결되어 있다면 예외 처리
        if (couple.getUsers().size() >= 2) {
            throw new CoupleCodeAlreadyUsedException();
        }

        // 커플에 두 번째 사용자 추가
        couple.addUser(user);
        coupleRepository.save(couple);
    }
}