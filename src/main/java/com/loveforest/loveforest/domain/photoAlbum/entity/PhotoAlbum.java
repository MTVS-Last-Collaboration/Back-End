package com.loveforest.loveforest.domain.photoAlbum.entity;

import com.loveforest.loveforest.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tbl_photo_album")
@Getter
@NoArgsConstructor
public class PhotoAlbum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;  // S3에 저장된 이미지 URL

    @Column(nullable = false)
    private String objectUrl; // S3에 저장된 3D 오브젝트 URL

    @Column(nullable = false)
    private Double positionX;

    @Column(nullable = false)
    private Double positionY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 생성자
    public PhotoAlbum(String imageUrl, String objectUrl, Double positionX, Double positionY, User user) {
        this.imageUrl = imageUrl;
        this.objectUrl = objectUrl;
        this.positionX = positionX;
        this.positionY = positionY;
        this.user = user;
    }
}