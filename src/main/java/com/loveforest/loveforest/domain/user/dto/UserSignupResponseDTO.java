package com.loveforest.loveforest.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupResponseDTO {
    @Schema(description = "회원가입한 유저 닉네임", example = "nickname1")
    private String nickname;

    @Schema(description = "커플 연동을 위한 커플코드", example = "aaa")
    private String coupleCode;
}
