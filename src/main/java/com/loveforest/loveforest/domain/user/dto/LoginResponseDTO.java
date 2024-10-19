package com.loveforest.loveforest.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;


/**
 * 로그인 응답 DTO
 * 사용자의 인증 토큰 정보를 담고 있다.
 */
public record LoginResponseDTO(
        @Schema(description = "액세스 토큰", example = "access_token_example")
        String accessToken,

        @Schema(description = "리프레시 토큰", example = "refresh_token_example")
        String refreshToken
) {
}

