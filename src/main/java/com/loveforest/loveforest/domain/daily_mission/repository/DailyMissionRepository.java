package com.loveforest.loveforest.domain.daily_mission.repository;

import com.loveforest.loveforest.domain.daily_mission.entity.DailyMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyMissionRepository extends JpaRepository<DailyMission, Long> {
    Optional<DailyMission> findByCouple_IdAndMissionDate(Long coupleId, LocalDate date);
    List<DailyMission> findByCouple_IdOrderByMissionDateDesc(Long coupleId);
    boolean existsByMissionDate(LocalDate date);

    // 존재 여부 확인 메서드 추가
    boolean existsByCouple_IdAndMissionDate(Long coupleId, LocalDate date);
}

