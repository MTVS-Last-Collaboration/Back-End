package com.loveforest.loveforest.domain.room.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_room_sharing")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomSharing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private boolean isShared;

    @Column(nullable = false)
    private LocalDateTime lastModified;

    public RoomSharing(Room room) {
        this.room = room;
        this.isShared = false;
        this.lastModified = LocalDateTime.now();
    }

    public void updateSharingStatus(boolean isShared) {
        this.isShared = isShared;
        this.lastModified = LocalDateTime.now();
    }
}
