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

    // 커플을 인자로 받는 생성자 추가
    public Room(Couple couple) {
        this.couple = couple;
        this.furnitureLayouts = new ArrayList<>(); // 가구 배치 초기화
    }


    // 방에 가구 배치 추가 메서드
    public void addFurnitureLayout(FurnitureLayout layout) {
        furnitureLayouts.add(layout);
        layout.setRoom(this); // 양방향 설정
    }
}