package com.loveforest.loveforest.domain.room.entity;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_room")
@Getter
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "couple_id")
    private Couple couple;

    @Column(name = "room_name", nullable = false)
    private String roomName;  // 방 이름 (필요시)

    // 생성자
    public Room(Couple couple) {
        this.couple = couple;
        this.roomName = "Room for couple " + couple.getId();  // 방 이름 자동 생성
    }
}