package com.loveforest.loveforest.domain.room.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_preset_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PresetRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "JSON", nullable = false)
    private String roomData;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public PresetRoom(String name, String roomData) {
        this.name = name;
        this.roomData = roomData;
        this.createdAt = LocalDateTime.now();
    }
}