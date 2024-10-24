package com.loveforest.loveforest.domain.couple.repository;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoupleRepository extends JpaRepository<Couple, Long> {

    Optional<Couple> findByCoupleCode(String coupleCode);

    // 특정 User가 속한 Couple을 찾는 메서드 정의
    Couple findByUsersContaining(User user);
}
