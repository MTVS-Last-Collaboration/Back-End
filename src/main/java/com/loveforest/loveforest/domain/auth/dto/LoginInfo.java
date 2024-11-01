package com.loveforest.loveforest.domain.auth.dto;

import com.loveforest.loveforest.domain.user.enums.Authority;
import lombok.Data;

@Data
public class LoginInfo {
    private String email;
    private Long userId;
    private String nickname;
    private Authority authorities;
    private Long coupleId;
}
