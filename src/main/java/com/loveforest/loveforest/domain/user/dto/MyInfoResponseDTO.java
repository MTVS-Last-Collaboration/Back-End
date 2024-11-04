package com.loveforest.loveforest.domain.user.dto;

import com.loveforest.loveforest.domain.user.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyInfoResponseDTO {
    private String nickname;
    private Gender gender;
    private LocalDate anniversaryDate;
    private String coupleCode;
}
