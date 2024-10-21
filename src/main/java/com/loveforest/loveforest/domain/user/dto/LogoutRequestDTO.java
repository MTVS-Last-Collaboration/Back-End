package com.loveforest.loveforest.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "로그아웃 요청 DTO")
public class LogoutRequestDTO {

    @Schema(description = "로그아웃시 삭제할 리프레시 토큰", example = "eyJraWQiOiJyZWZyZXNoIiwiYWxnIjoiSFMyNTYifQ.eyJzdWIiOiJuaWNrbmFtZTEiLCJyb2xlIjoiVVNFUiIsImlzcyI6IkNvdXJzZU1ha2VyIiwiaWF0IjoxNzI0MDcwMTgzLCJleHAiOjE3MjQxNTY1ODN9.Kq3IxNvjKXr5IUunCWraKVisqBr37E03t8VtBLFUhqU")
    private String refreshToken;
}

