package com.loveforest.loveforest.domain.room.entity;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.room.enums.RoomStateSource;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_room_collection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", nullable = false, unique = true)
    private Couple couple;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectionRoom> savedRooms = new ArrayList<>();

    public RoomCollection(Couple couple) {
        this.couple = couple;
    }

    public void addRoom(Room room, RoomStateSource source) {
        CollectionRoom collectionRoom = new CollectionRoom(this, room.serializeState(), source);
        this.savedRooms.add(collectionRoom);
    }
}