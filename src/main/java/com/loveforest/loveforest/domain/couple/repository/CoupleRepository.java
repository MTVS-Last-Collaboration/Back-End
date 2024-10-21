package com.loveforest.loveforest.domain.couple.repository;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoupleRepository extends JpaRepository<Couple, Long> {

    Optional<Couple> findByCoupleCode(String coupleCode);
}
