package com.loveforest.loveforest.domain.room.repository;

import com.loveforest.loveforest.domain.room.entity.Wallpaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WallpaperRepository extends JpaRepository<Wallpaper, Long> {
    Optional<Wallpaper> findByWallpaperNumber(int wallpaperNumber);
    List<Wallpaper> findAllByOrderByWallpaperNumberAsc();
}
