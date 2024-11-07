package com.loveforest.loveforest.domain.couple.entity;

import com.loveforest.loveforest.BaseTimeEntity;
import com.loveforest.loveforest.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_couple")
@Getter
@NoArgsConstructor
public class Couple extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "couple_id")
    private Long id;

    @Column(name = "couple_code", nullable = false, unique = true)
    private String coupleCode;

    @Column(nullable = false)
    private int points = 0;  // 커플 포인트 필드 추가


    // 사용자들 (1:N 관계로 한 커플에 2명의 사용자 연결)
    @OneToMany(mappedBy = "couple", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, orphanRemoval = false)
    private List<User> users = new ArrayList<>();

    public Couple(String coupleCode) {
        this.coupleCode = coupleCode;
    }

    // 사용자 추가 메서드 (연관 관계 편의 메서드)
    public void addUser(User user) {
        if (!users.contains(user)) {  // 중복 추가 방지
            users.add(user);
            user.setCouple(this);  // 사용자와 커플 간의 관계 설정
        }
    }

    public void addPoints(int points) {
        this.points += points;
    }
}
