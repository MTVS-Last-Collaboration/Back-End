package com.loveforest.loveforest.domain.auth.jwt;

import com.loveforest.loveforest.domain.auth.jwt.exception.InvalidAccessTokenException;
import com.loveforest.loveforest.domain.user.entity.User;
import com.loveforest.loveforest.domain.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final Key secretKey;
    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;
    private final UserRepository userRepository;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration,
            UserRepository userRepository
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationTime = accessTokenExpiration * 1000L;
        this.refreshTokenExpirationTime = refreshTokenExpiration * 1000L;
        this.userRepository = userRepository;
    }

    /**
     * 액세스 토큰 생성 메서드
     *
     * @param email 사용자 이메일
     * @return 생성된 액세스 토큰 문자열
     * @explain 주어진 이메일을 바탕으로 만료 기간 15분짜리 액세스 토큰을 생성하여 반환합니다.
     */
    public String createAccessToken(String email) {
        return createToken(email, accessTokenExpirationTime, TYPE_ACCESS);
    }

    /**
     * 리프레시 토큰 생성 메서드
     *
     * @param email 사용자 이메일
     * @return 생성된 리프레시 토큰 문자열
     * @explain 주어진 이메일을 바탕으로 만료 기간 7일짜리 리프레시 토큰을 생성하여 반환합니다.
     */
    public String createRefreshToken(String email) {
        return createToken(email, refreshTokenExpirationTime, TYPE_REFRESH);
    }

    /**
     * 토큰 생성 메서드 (공통 로직)
     *
     * @param email 사용자 이메일
     * @param expirationTime 토큰 만료 시간
     * @param tokenType 토큰 타입 (액세스 또는 리프레시)
     * @return 생성된 JWT 토큰 문자열
     */
    private String createToken(String email, long expirationTime, String tokenType) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("type", tokenType);
        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰의 유효성을 검사하는 메서드
     *
     * @param token 검사할 JWT 토큰
     * @return 유효한 경우 true, 그렇지 않은 경우 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.info("JWT 토큰 검증 실패: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰에서 이메일(subject)를 추출하는 메서드
     *
     * @param token JWT 토큰
     * @return 토큰에서 추출된 이메일 (subject)
     */
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * 토큰의 만료 여부를 확인하는 메서드
     *
     * @param token JWT 토큰
     * @return 토큰이 만료된 경우 true, 그렇지 않은 경우 false
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Date expirationDate = claims.getExpiration();
            return expirationDate.before(new Date());
        } catch (ExpiredJwtException e) {
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

    /**
     * JWT 토큰을 바탕으로 인증 정보를 가져오는 메서드
     *
     * @param token JWT 토큰
     * @return 인증 정보 (Authentication 객체)
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY, String.class).split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User user = userRepository.findByEmail(claims.getSubject())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return new UsernamePasswordAuthenticationToken(user, token, authorities);
    }

    /**
     * 요청에서 토큰을 추출하는 메서드
     *
     * @param request HTTP 요청 객체
     * @param tokenType 토큰 타입 (accessToken 또는 refreshToken)
     * @return 추출된 토큰 문자열
     */
    public String resolveToken(HttpServletRequest request, String tokenType) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 주어진 토큰이 리프레시 토큰인지 확인하는 메서드
     *
     * @param token JWT 토큰
     * @return 리프레시 토큰인 경우 true, 그렇지 않은 경우 false
     */
    public boolean isRefreshToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return TYPE_REFRESH.equals(claims.get("type"));
    }

    /**
     * 토큰 재발급 메서드
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로 발급된 액세스 토큰
     * @throws InvalidAccessTokenException 리프레시 토큰이 유효하지 않거나 만료된 경우 예외 발생
     */
    public String reissueAccessToken(String refreshToken) {
        if (validateToken(refreshToken) && isRefreshToken(refreshToken) && !isTokenExpired(refreshToken)) {
            String email = getEmailFromToken(refreshToken);
            return createAccessToken(email);
        } else {
            throw new InvalidAccessTokenException();
        }
    }
}
