package com.loveforest.loveforest.domain.auth.jwt.refreshToken;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.TimeUnit;

@Repository
public class RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final long refreshTokenExpirationTime;

    public RefreshTokenRepository(
            RedisTemplate<String, String> redisTemplate,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenExpirationTime = refreshTokenExpiration;
    }

    // 리프레시 토큰 저장
    public void saveRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(email, refreshToken, refreshTokenExpirationTime, TimeUnit.SECONDS);
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
