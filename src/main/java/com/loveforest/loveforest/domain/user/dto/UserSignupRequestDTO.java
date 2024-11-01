package com.loveforest.loveforest.domain.user.dto;

import com.loveforest.loveforest.domain.user.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserSignupRequestDTO {

    @Schema(description = "회원가입에 사용될 이메일 주소",
            example = "example@example.com",
            required = true,
            type = "String")
    @Email(message = "이메일 형식이 잘못되었습니다.")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    private String email;

    @Schema(description = "사용자의 실제 이름",
            example = "정석원",
            required = true,
            type = "String")
    @NotBlank(message = "유저명은 필수 입력값입니다.")
    @Size(min = 2, max = 10, message = "유저명은 2자 이상, 10자로 입력해야 합니다.")
    private String username;

    @Schema(description = "회원가입 시 설정할 비밀번호",
            example = "1234",
            required = true,
            type = "String")
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 4, message = "비밀번호는 최소 4자 이상이어야 합니다.")
    private String password;

    @Schema(description = "회원가입 시 설정할 닉네임",
            example = "오승훈훈훈",
            required = true,
            type = "String")
    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    @Size(min = 1, max = 10, message = "닉네임은 최소 1자 이상, 최대 10자 이하로 입력해야 합니다.")
    private String nickname;

    @Schema(description = "회원가입 시 설정할 성별",
            example = "MALE",
            required = true,
            type = "String")
    @NotBlank(message = "성별은 필수 입력값입니다.")
    private Gender gender;

    @Schema(description = "기념일 날짜",
            example = "2023-01-01",
            required = true,
            type = "String", format = "date")
    @NotNull(message = "기념일은 필수 입력값입니다.")
    private LocalDate anniversary;

}
