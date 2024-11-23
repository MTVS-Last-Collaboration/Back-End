package com.loveforest.loveforest.domain.user.dto;

import com.loveforest.loveforest.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private String coupleCode;
    private String myNickname;
    private String partnerNickname;
}
