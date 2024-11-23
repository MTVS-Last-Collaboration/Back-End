package com.loveforest.loveforest.domain.room.repository;

import com.loveforest.loveforest.domain.room.entity.CollectionRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRoomRepository extends JpaRepository<CollectionRoom, Long> {
    List<CollectionRoom> findByCollectionIdOrderBySavedAtDesc(Long collectionId);

    @Query("SELECT cr FROM CollectionRoom cr WHERE cr.collection.couple.id = :coupleId")
    List<CollectionRoom> findByCollectionCoupleId(@Param("coupleId") Long coupleId);
}
