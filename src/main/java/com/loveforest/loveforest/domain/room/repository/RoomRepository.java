package com.loveforest.loveforest.domain.room.repository;

import com.loveforest.loveforest.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByCoupleId(Long coupleId); // 커플 ID로 방 조회

    @Query("SELECT r FROM Room r JOIN r.sharing s WHERE s.isShared = true")
    List<Room> findBySharing_IsSharedTrue();
}
