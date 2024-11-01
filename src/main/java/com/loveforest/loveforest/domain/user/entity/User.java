package com.loveforest.loveforest.domain.user.entity;

import com.loveforest.loveforest.BaseTimeEntity;
import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.user.enums.Gender;
import com.loveforest.loveforest.domain.user.enums.Authority;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;


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

    /**
     * -- SETTER --
     * 커플 설정 메서드
     *
     * @param couple
     */
    // 커플 ID (Foreign Key)
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)  // 지연 로딩을 사용하는 이유 : 데이터를 실제로 사용할 때까지 데이터베이스에서 조회하지 않는다.
    @JoinColumn(name = "couple_id")
    private Couple couple;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'USER'")
    private Authority authority;

    @Column(name = "anniversary_date")
    private LocalDate anniversaryDate;



    /**
     * User 생성자.
     * 기본적으로 role은 USER로 설정된다.
     */
    @Builder
    public User(String email, String username, String password, String nickname, Gender gender , Authority authority , LocalDate anniversaryDate) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.gender = gender;
        // role이 null이면 기본값으로 USER 설정
        this.authority = authority != null ? authority : Authority.USER;
        this.anniversaryDate = anniversaryDate;
    }

    /** 닉네임 변경 메서드
     *
     * @param newNickname
     */
    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }

}
