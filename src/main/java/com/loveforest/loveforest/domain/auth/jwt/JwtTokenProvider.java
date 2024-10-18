package com.loveforest.loveforest.domain.auth.jwt;

import com.loveforest.loveforest.domain.auth.jwt.exception.InvalidAccessTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 15;  // 액세스 토큰: 15분
    private final long refreshTokenExpirationTime;

    // secret 값을 주입
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenExpirationTime = refreshTokenExpiration * 1000; // 밀리초로 변환
    }

    /**
     * 액세스 토큰 생성 메서드
     *
     * @param email 사용자 이메일
     * @return 생성된 액세스 토큰 문자열
     * @explain 주어진 이메일을 바탕으로 만료 기간 15분짜리 액세스 토큰을 생성하여 반환합니다.
     */
    public String createAccessToken(String email) {
        log.info("액세스 토큰 생성 요청 - 이메일: {}", email);
        return createToken(email, ACCESS_TOKEN_EXPIRATION_TIME);
    }

    /**
     * 리프레시 토큰 생성 메서드
     *
     * @param email 사용자 이메일
     * @return 생성된 리프레시 토큰 문자열
     * @explain 주어진 이메일을 바탕으로 만료 기간 7일짜리 리프레시 토큰을 생성하여 반환합니다.
     */
    public String createRefreshToken(String email) {
        log.info("리프레시 토큰 생성 요청 - 이메일: {}", email);
        return createToken(email, refreshTokenExpirationTime);
    }

    /**
     * 공통적인 JWT 토큰 생성 메서드
     *
     * @param email 사용자 이메일
     * @param expirationTime 토큰 만료 시간 (밀리초)
     * @return 생성된 토큰 문자열
     * @explain 주어진 이메일과 만료 시간을 기준으로 JWT 토큰을 생성하여 반환합니다.
     */
    private String createToken(String email, long expirationTime) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)  // secretKey를 사용하여 서명
                .compact();
    }

    /**
     * JWT 토큰에서 이메일 추출 메서드
     *
     * @param token JWT 토큰
     * @return 추출된 이메일 (subject 값)
     * @explain 주어진 JWT 토큰에서 subject로 설정된 이메일을 추출하여 반환합니다.
     */
    public String getEmailFromToken(String token) {
        log.debug("토큰에서 이메일 추출 - 토큰: {}", token);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * 토큰 유효성 검사
     *
     * @param token 검증할 토큰
     * @return 토큰이 유효하면 true, 유효하지 않으면 false
     * @explain 토큰이 유효한지 여부를 검증하고, 유효하지 않으면 false를 반환합니다.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            log.debug("토큰 유효성 검사 통과 - 토큰: {}", token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("토큰이 만료되었습니다 - 토큰: {}", token);
            throw new InvalidAccessTokenException();  // 구체적인 만료 에러 반환
        } catch (Exception e) {
            log.error("유효하지 않은 토큰입니다 - 토큰: {}", token, e);
            return false;
        }
    }

    /**
     * 토큰 만료 여부 검사 메서드
     *
     * @param token 검사할 토큰
     * @return 토큰이 만료되었으면 true, 그렇지 않으면 false
     * @explain 토큰이 만료되었는지 여부를 확인합니다.
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            boolean isExpired = claims.getExpiration().before(new Date());
            log.debug("토큰 만료 여부 검사 - 토큰 만료 상태: {}", isExpired);
            return isExpired;
        } catch (ExpiredJwtException e) {
            log.warn("토큰이 만료되었습니다 - 토큰: {}", token);
            return true;
        } catch (Exception e) {
            log.error("토큰 만료 검사 중 오류가 발생했습니다 - 토큰: {}", token, e);
            return true;
        }
    }

    /**
     * 액세스 토큰의 유효성 및 만료 상태 검사 메서드
     *
     * @param accessToken 액세스 토큰
     * @throws InvalidAccessTokenException 액세스 토큰이 유효하지 않거나 만료된 경우 예외 발생
     * @explain 주어진 액세스 토큰이 유효한지 확인하고, 만료된 경우 예외를 던집니다.
     */
    public void validateAccessToken(String accessToken) {
        log.info("액세스 토큰 유효성 및 만료 상태 검증 요청 - 토큰: {}", accessToken);

        if (!validateToken(accessToken)) {
            log.warn("액세스 토큰이 유효하지 않음 - 토큰: {}", accessToken);
            throw new InvalidAccessTokenException();
        }

        if (isTokenExpired(accessToken)) {
            log.warn("액세스 토큰이 만료됨 - 토큰: {}", accessToken);
            throw new InvalidAccessTokenException();
        }

        log.debug("액세스 토큰이 유효함 - 토큰: {}", accessToken);
    }
}
