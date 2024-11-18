package com.loveforest.loveforest.domain.photoAlbum.entity;

import com.loveforest.loveforest.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_photo_album")
@Getter
@NoArgsConstructor
public class PhotoAlbum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "photo_date", nullable = false)
    private LocalDate photoDate;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(nullable = true)
    private String objectUrl; // S3에 저장된 3D .obj 파일 URL

    @Column(nullable = true)
    private String pngUrl; // S3에 저장된 .png 파일 URL (텍스처)

    @Column(nullable = true)
    private String materialUrl; // S3에 저장된 .mtl 파일 URL (재질)

    @Column(nullable = true)
    private Double positionX;

    @Column(nullable = true)
    private Double positionY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 생성자 수정
    public PhotoAlbum(String title, String content, LocalDate photoDate,
                      String imageUrl, String objectUrl, String pngUrl, String materialUrl,
                      Double positionX, Double positionY, User user) {
        this.title = title;
        this.content = content;
        this.photoDate = photoDate;
        this.imageUrl = imageUrl;
        this.objectUrl = objectUrl;
        this.pngUrl = pngUrl;
        this.materialUrl = materialUrl;
        this.positionX = positionX;
        this.positionY = positionY;
        this.user = user;
    }

    public void updateModelUrlsAndPosition(String objUrl, String pngUrl, String mtlUrl, Double positionX, Double positionY) {
        this.objectUrl = objUrl;
        this.pngUrl = pngUrl;
        this.materialUrl = mtlUrl;
        this.positionX = positionX;
        this.positionY = positionY;
    }
}