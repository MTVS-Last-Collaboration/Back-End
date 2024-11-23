package com.loveforest.loveforest.domain.room.repository;

import com.loveforest.loveforest.domain.room.entity.RoomCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomCollectionRepository extends JpaRepository<RoomCollection, Long> {
    Optional<RoomCollection> findByCoupleId(Long coupleId);
}
