package com.loveforest.loveforest.domain.room.entity;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_room")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;  // 커플과 연결된 방

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FurnitureLayout> furnitureLayouts = new ArrayList<>(); // 방 안의 가구 배치 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallpaper_id")
    private Wallpaper wallpaper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id")
    private Floor floor;


    // 커플을 인자로 받는 생성자 추가
    public Room(Couple couple) {
        this.couple = couple;
        this.furnitureLayouts = new ArrayList<>(); // 가구 배치 초기화
    }

    /**
     * 벽지 설정 메서드
     */
    public void setWallpaper(Wallpaper wallpaper) {
        this.wallpaper = wallpaper;
    }

    /**
     * 바닥 설정 메서드
     */
    public void setFloor(Floor floor) {
        this.floor = floor;
    }


    // 방에 가구 배치 추가 메서드
    public void addFurnitureLayout(FurnitureLayout layout) {
        furnitureLayouts.add(layout);
        layout.setRoom(this); // 양방향 설정
    }

    public void removeFurnitureLayout(FurnitureLayout layout) {
        furnitureLayouts.remove(layout);
        layout.setRoom(null);  // 양방향 관계 해제
    }
}