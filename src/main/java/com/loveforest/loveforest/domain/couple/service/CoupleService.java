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

        // 커플 코드 생성
        String coupleCode = UUID.randomUUID().toString();

        // 커플 엔티티 생성 후 첫 번째 사용자와 연결
        Couple couple = new Couple(coupleCode);
        couple.addUser(user);
        coupleRepository.save(couple);

        return coupleCode;  // 생성된 커플 코드 반환
    }

    // 커플 코드로 두 번째 사용자를 커플에 연동
    public void joinCouple(Long userId, String coupleCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

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