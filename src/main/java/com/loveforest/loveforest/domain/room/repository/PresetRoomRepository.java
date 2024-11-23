package com.loveforest.loveforest.domain.room.repository;

import com.loveforest.loveforest.domain.room.entity.PresetRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PresetRoomRepository extends JpaRepository<PresetRoom, Long> {
    List<PresetRoom> findAllByOrderByCreatedAtDesc();
}