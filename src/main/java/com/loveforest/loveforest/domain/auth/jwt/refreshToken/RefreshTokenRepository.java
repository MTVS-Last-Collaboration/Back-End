package com.loveforest.loveforest.domain.auth.jwt.refreshToken;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public RefreshTokenRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 리프레시 토큰 저장
    public void saveRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(email, refreshToken, 7, TimeUnit.DAYS);
    }

    // 리프레시 토큰 조회
    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    // 리프레시 토큰 삭제
    public void deleteRefreshToken(String email) {
        redisTemplate.delete(email);
    }
}
