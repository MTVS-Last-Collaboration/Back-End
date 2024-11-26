package com.loveforest.loveforest.domain.room.entity;

import com.loveforest.loveforest.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_preset_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PresetRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;  // 프리셋 이름

    // 벽지 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallpaper_id")
    private Wallpaper wallpaper;

    // 바닥 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id")
    private Floor floor;

    // 가구 배치 정보
    @OneToMany(mappedBy = "presetRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PresetFurnitureLayout> furnitureLayouts = new ArrayList<>();

    @Column(nullable = true)
    private String thumbnailUrl;

    @Builder
    public PresetRoom(String name, Wallpaper wallpaper, Floor floor, String thumbnailUrl) {
        this.name = name;
        this.wallpaper = wallpaper;
        this.floor = floor;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void addFurnitureLayout(PresetFurnitureLayout layout) {
        this.furnitureLayouts.add(layout);
        layout.setPresetRoom(this);
    }

    public void updateThumbnail(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}