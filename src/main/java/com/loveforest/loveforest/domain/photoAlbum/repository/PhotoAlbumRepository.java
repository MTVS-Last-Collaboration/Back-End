package com.loveforest.loveforest.domain.photoAlbum.repository;

import com.loveforest.loveforest.domain.photoAlbum.entity.PhotoAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotoAlbumRepository extends JpaRepository<PhotoAlbum, Long> {

    // 특정 사용자의 모든 사진 조회 (N+1 문제 방지를 위한 fetch join 사용)
    @Query("SELECT p FROM PhotoAlbum p JOIN FETCH p.user WHERE p.user.id = :userId ORDER BY p.id DESC")
    List<PhotoAlbum> findByUserId(@Param("userId") Long userId);

    List<PhotoAlbum> findByCoupleIdOrderByPhotoDateDesc(Long coupleId);

}