package com.loveforest.loveforest.domain.user.entity;

import com.loveforest.loveforest.BaseTimeEntity;
import com.loveforest.loveforest.domain.couple.entity.Couple;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * User 엔티티 클래스
 * 사용자의 기본 정보를 나타낸다.
 */
@Entity
@Table(name = "tbl_user")
@Getter
@NoArgsConstructor
public class User extends BaseTimeEntity {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "email",nullable = false, unique = true)
    private String email;


    @Column(name = "username",nullable = false)
    private String username;

    @Column(name = "password",nullable = false)
    private String password;

    @Column(name = "nickname",nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 커플 ID (Foreign Key)
    @ManyToOne(fetch = FetchType.LAZY)  // 지연 로딩을 사용하는 이유 : 데이터를 실제로 사용할 때까지 데이터베이스에서 조회하지 않는다.
    @JoinColumn(name = "couple_id")
    private Couple couple;

    // Role 열거형 정의
    public enum Role {
        USER, ADMIN
    }

    /**
     * User 생성자.
     * 기본적으로 role은 USER로 설정된다.
     */
    @Builder
    public User(String email, String username, String password, String nickname, Role role) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        // role이 null이면 기본값으로 USER 설정
        this.role = role != null ? role : Role.USER;
    }

    /** 닉네임 변경 메서드
     *
     * @param newNickname
     */
    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }

    /** 커플 설정 메서드
     *
     * @param couple
     */
    public void setCouple(Couple couple) {
        this.couple = couple;
    }
}