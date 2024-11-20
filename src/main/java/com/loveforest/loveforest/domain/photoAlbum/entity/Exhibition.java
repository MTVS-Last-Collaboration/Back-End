package com.loveforest.loveforest.domain.photoAlbum.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_exhibition")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exhibition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", unique = true)
    private PhotoAlbum photo;

    @Column(nullable = false)
    private Integer positionX;

    @Column(nullable = false)
    private Integer positionY;

    @Column(nullable = false)
    private LocalDateTime exhibitedAt;

    @Builder
    public Exhibition(PhotoAlbum photo, Integer positionX, Integer positionY, LocalDateTime exhibitedAt) {
        this.photo = photo;
        this.positionX = positionX;
        this.positionY = positionY;
        this.exhibitedAt = exhibitedAt;
    }
}
