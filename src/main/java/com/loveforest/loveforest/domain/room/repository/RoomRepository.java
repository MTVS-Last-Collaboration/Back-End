package com.loveforest.loveforest.domain.room.repository;

import com.loveforest.loveforest.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByCoupleId(Long coupleId); // 커플 ID로 방 조회
}
