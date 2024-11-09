package com.loveforest.loveforest.domain.room.repository;

import com.loveforest.loveforest.domain.room.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {
    Optional<Floor> findByFloorNumber(int floorNumber);
    List<Floor> findAllByOrderByFloorNumberAsc();
}