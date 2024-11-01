package com.loveforest.loveforest;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.user.dto.UserSignupRequestDTO;
import com.loveforest.loveforest.domain.user.enums.Authority;
import com.loveforest.loveforest.domain.user.enums.Gender;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import com.loveforest.loveforest.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class MockDataLoader {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadMockData() {
        return args -> {
            // 남자 회원 생성
            createUserIfNotExists("1@example.com", "남자회원", "1234", "남자", Gender.MALE, LocalDate.parse("2023-01-01"));

            // 여자 회원 생성
            createUserIfNotExists("2@example.com", "여자회원", "1234", "여자", Gender.FEMALE, LocalDate.parse("2023-01-01"));
        };
    }

    private void createUserIfNotExists(String email, String username, String password, String nickname, Gender gender,LocalDate anniversary) {
        if (!userRepository.existsByEmail(email)) {
            UserSignupRequestDTO requestDTO = UserSignupRequestDTO.builder()
                    .email(email)
                    .username(username)
                    .password(password)
                    .nickname(nickname)
                    .gender(gender)
                    .anniversary(anniversary)
                    .build();
            userService.signUp(requestDTO);
        }
    }
}
