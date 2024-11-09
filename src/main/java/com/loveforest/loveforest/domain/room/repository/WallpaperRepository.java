package com.loveforest.loveforest.domain.room.repository;

import com.loveforest.loveforest.domain.room.entity.Wallpaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface WallpaperRepository extends JpaRepository<Wallpaper, Long> {
}
