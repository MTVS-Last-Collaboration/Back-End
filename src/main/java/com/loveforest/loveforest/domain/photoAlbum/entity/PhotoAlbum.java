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

    @Column(nullable = false)
    private String title; // 제목 추가

    @Column(nullable = false)
    private String content; // 내용 추가

    @Column(nullable = false)
    private LocalDate photoDate; // 날짜 추가


    @Column(nullable = false)
    private String imageUrl;  // S3에 저장된 원본 이미지 URL

    @Column(nullable = false)
    private String objectUrl; // S3에 저장된 3D .obj 파일 URL

    @Column(nullable = false)
    private String pngUrl; // S3에 저장된 .png 파일 URL (텍스처)

    @Column(nullable = false)
    private String materialUrl; // S3에 저장된 .mtl 파일 URL (재질)

    @Column(nullable = false)
    private Double positionX;

    @Column(nullable = false)
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