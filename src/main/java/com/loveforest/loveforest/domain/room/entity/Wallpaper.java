package com.loveforest.loveforest.domain.room.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_wallpaper")
@Getter
@NoArgsConstructor
public class Wallpaper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int wallpaperNumber;  // 1~11까지의 벽지 번호

    @Column(nullable = false)
    private String name;

    @Builder
    public Wallpaper(int wallpaperNumber, String name) {
        this.wallpaperNumber = wallpaperNumber;
        this.name = name;
    }
}
