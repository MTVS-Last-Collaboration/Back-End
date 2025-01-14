package com.loveforest.loveforest.domain.user.repository;

import com.loveforest.loveforest.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

//    Optional<User> findByEmail(String email);
    User findByEmail(String email);

    Optional<User> findByNickname(String nickname);
}
