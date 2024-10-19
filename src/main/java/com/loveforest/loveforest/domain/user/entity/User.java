package com.loveforest.loveforest.domain.user.entity;

import com.loveforest.loveforest.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * User 엔티티 클래스
 * 사용자의 기본 정보를 나타낸다.
 */
@Entity(name = "tbl_user")
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
    // 닉네임 변경 메서드
    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }
}
