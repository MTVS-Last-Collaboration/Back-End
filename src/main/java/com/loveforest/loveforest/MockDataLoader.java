package com.loveforest.loveforest;

import com.loveforest.loveforest.domain.user.enums.Authority;
import com.loveforest.loveforest.domain.user.enums.Gender;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import com.loveforest.loveforest.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class MockDataLoader {

    private final UserService userService;
    private final UserRepository userRepository;

    @Bean
    public CommandLineRunner loadMockData() {
        return args -> {
            // 남자 회원 생성
            createUserIfNotExists("1@example.com", "남자회원", "1234", "남자", Gender.MALE, Authority.USER, LocalDate.parse("2023-01-01"));

            // 여자 회원 생성
            createUserIfNotExists("2@example.com", "여자회원", "1234", "여자", Gender.FEMALE, Authority.USER, LocalDate.parse("2023-01-01"));
        };
    }

    private void createUserIfNotExists(String email, String username, String password, String nickname, Gender gender, Authority authority, LocalDate anniversary) {
        if (!userRepository.existsByEmail(email)) {
            userService.createUser(email, username, password, nickname, gender, authority, anniversary);
        }
    }
}
