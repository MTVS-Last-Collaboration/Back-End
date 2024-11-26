package com.loveforest.loveforest.domain.room.entity;

import com.loveforest.loveforest.domain.room.enums.RoomStateSource;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_collection_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class CollectionRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private RoomCollection collection;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStateSource source;

    @Column(columnDefinition = "JSON", nullable = false)
    private String roomData;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(nullable = false)
    private LocalDateTime savedAt;

    public CollectionRoom(RoomCollection collection, String roomData, RoomStateSource source) {
        this.collection = collection;
        this.roomData = roomData;
        this.source = source;
        this.savedAt = LocalDateTime.now();
    }

    public void updateRoomData(String newRoomData) {
        this.roomData = newRoomData;
        this.savedAt = LocalDateTime.now();
    }
}