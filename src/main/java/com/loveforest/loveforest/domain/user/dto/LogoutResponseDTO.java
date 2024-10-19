package com.loveforest.loveforest.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;


/**
 * 로그아웃 응답 DTO
 * 사용자의 이메일 담고 있다.
 */
public record LogoutResponseDTO(
        @Schema(description = "로그아웃한 회원의 이메일", example = "example@example.com")
        String email
) {
}
