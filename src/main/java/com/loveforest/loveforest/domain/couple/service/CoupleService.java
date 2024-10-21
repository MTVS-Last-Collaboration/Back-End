package com.loveforest.loveforest.domain.couple.service;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.exception.CoupleCodeAlreadyUsedException;
import com.loveforest.loveforest.domain.couple.exception.CoupleNotFoundException;
import com.loveforest.loveforest.domain.couple.repository.CoupleRepository;
import com.loveforest.loveforest.domain.room.entity.Room;
import com.loveforest.loveforest.domain.room.repository.RoomRepository;
import com.loveforest.loveforest.domain.user.dto.UserSignupRequestDTO;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CoupleService {

    private final CoupleRepository coupleRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    // 커플 코드로 두 번째 사용자 연결 및 방 배정
    public void joinCouple(String coupleCode, UserSignupRequestDTO request) {
        // 커플 코드로 커플 찾기
        Couple couple = coupleRepository.findByCoupleCode(coupleCode)
                .orElseThrow(CoupleNotFoundException::new);

        // 이미 두 명의 사용자가 있다면 예외 발생
        if (couple.getUsers().size() >= 2) {
            throw new CoupleCodeAlreadyUsedException();
        }

        // 새로운 사용자 생성
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .build();

        // 커플과 사용자 연결
        couple.addUser(user);
        coupleRepository.save(couple);

        // 커플이 완성되면 방 배정
        if (couple.getUsers().size() == 2) {
            Room room = new Room(couple);
            roomRepository.save(room);
        }
    }
}