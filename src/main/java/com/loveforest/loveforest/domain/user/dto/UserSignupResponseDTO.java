package com.loveforest.loveforest.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Schema(description = "회원가입 응답 DTO")
public class UserSignupResponseDTO {
    @Schema(description = "회원가입 후 생성된 유저의 닉네임",
            example = "nickname1",
            required = true,
            type = "String")
    private String nickname;

    @Schema(description = "커플 연동을 위한 고유 커플 코드",
            example = "123e4567-e89b-12d3-a456-426614174000",
            required = true,
            type = "String")
    private String coupleCode;
}
