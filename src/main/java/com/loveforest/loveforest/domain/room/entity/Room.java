package com.loveforest.loveforest.domain.room.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.room.dto.FurnitureLayoutDTO;
import com.loveforest.loveforest.domain.room.dto.RoomStateDTO;
import com.loveforest.loveforest.domain.room.exception.FurnitureNotFoundException;
import com.loveforest.loveforest.domain.room.exception.RoomSerializationException;
import com.loveforest.loveforest.domain.room.repository.FloorRepository;
import com.loveforest.loveforest.domain.room.repository.FurnitureRepository;
import com.loveforest.loveforest.domain.room.repository.WallpaperRepository;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "tbl_room")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @OneToOne
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;  // 커플과 연결된 방

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FurnitureLayout> furnitureLayouts = new ArrayList<>(); // 방 안의 가구 배치 정보

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallpaper_id")
    private Wallpaper wallpaper;


    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id")
    private Floor floor;

    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private RoomSharing sharing;

    // 공유 설정 메서드
    public void updateSharing(boolean isShared) {
        if (this.sharing == null) {
            this.sharing = new RoomSharing(this);
        }
        this.sharing.updateSharingStatus(isShared);
    }

    // JSON으로 현재 상태 직렬화
    @JsonIgnore
    public String serializeState() {
        RoomStateDTO state = new RoomStateDTO(
                this.wallpaper != null ? this.wallpaper.getId() : null, // Wallpaper ID
                this.floor != null ? this.floor.getId() : null,         // Floor ID
                this.furnitureLayouts.stream()
                        .map(FurnitureLayoutDTO::from)
                        .collect(Collectors.toList())
        );
        try {
            return new ObjectMapper().writeValueAsString(state);
        } catch (JsonProcessingException e) {
            throw new RoomSerializationException();
        }
    }



    @Transactional
    public void restoreState(String roomData, FurnitureRepository furnitureRepository,
                             WallpaperRepository wallpaperRepository, FloorRepository floorRepository) {
        try {
            RoomStateDTO state = new ObjectMapper().readValue(roomData, RoomStateDTO.class);

            // Wallpaper 복원
            if (state.getWallpaperId() != null) {
                this.wallpaper = wallpaperRepository.findById(state.getWallpaperId())
                        .orElseThrow(RoomSerializationException::new);
            } else {
                this.wallpaper = null;
            }

            // Floor 복원
            if (state.getFloorId() != null) {
                this.floor = floorRepository.findById(state.getFloorId())
                        .orElseThrow(RoomSerializationException::new);
            } else {
                this.floor = null;
            }

            // FurnitureLayouts 복원
            this.furnitureLayouts.clear();
            state.getFurnitureLayouts().forEach(layoutDTO -> {
                Furniture furniture = furnitureRepository.findById(layoutDTO.getFurnitureId())
                        .orElseThrow(FurnitureNotFoundException::new);
                this.furnitureLayouts.add(layoutDTO.toEntity(this, furniture));
            });
        } catch (JsonProcessingException e) {
            throw new RoomSerializationException();
        }
    }



    // 커플을 인자로 받는 생성자 추가
    public Room(Couple couple) {
        this.couple = couple;
        this.furnitureLayouts = new ArrayList<>(); // 가구 배치 초기화
    }

    public boolean isShared() {
        return sharing != null && sharing.isShared();
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