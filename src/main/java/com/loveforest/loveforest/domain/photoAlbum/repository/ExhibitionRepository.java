package com.loveforest.loveforest.domain.photoAlbum.repository;

import com.loveforest.loveforest.domain.photoAlbum.entity.Exhibition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {
    boolean existsByPhotoId(Long photoId);
    Optional<Exhibition> findByPhotoId(Long photoId);

    @Query("SELECT e FROM Exhibition e WHERE e.photo.couple.id = :coupleId")
    List<Exhibition> findAllByCoupleId(@Param("coupleId") Long coupleId);
}
