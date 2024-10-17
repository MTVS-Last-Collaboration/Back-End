package com.loveforest.loveforest.domain.auth.dto;

import lombok.Data;

@Data
public class LoginInfo {
    private Long userId;
    private String username;
    private String nickname;
}
