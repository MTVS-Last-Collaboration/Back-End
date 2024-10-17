package com.loveforest.loveforest.domain.user.entity;

import com.loveforest.loveforest.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;

@Entity(name = "tbl_user")
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;
}
