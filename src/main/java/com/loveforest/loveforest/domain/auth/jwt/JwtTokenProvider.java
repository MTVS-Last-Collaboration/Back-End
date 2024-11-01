package com.loveforest.loveforest.domain.auth.jwt;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.auth.jwt.exception.InvalidAccessTokenException;
import com.loveforest.loveforest.domain.user.enums.Authority;
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
     * JWT 토큰 생성 메서드 (공통 로직)
     *
     * @param email 사용자 이메일
     * @param expirationTime 토큰 만료 시간 (밀리초)
     * @param tokenType 토큰 타입 (액세스 또는 리프레시)
     * @param userId 사용자 ID
     * @param nickname 사용자 닉네임
     * @return 생성된 JWT 토큰 문자열
     * @explain 주어진 사용자 정보를 바탕으로 JWT 토큰을 생성하여 반환합니다.
     */
    private String createToken(String email, long expirationTime, String tokenType, Long userId, String nickname, String authorities, Long coupleId) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("type", tokenType);
        claims.put("userId", userId); // 추가된 사용자 정보
        claims.put("nickname", nickname); // 추가된 사용자 정보
        claims.put(AUTHORITIES_KEY, authorities); // 권한 추가
        claims.put("coupleId", coupleId);
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
     * 액세스 토큰 생성 메서드
     *
     * @param email 사용자 이메일
     * @param userId 사용자 ID
     * @param nickname 사용자 닉네임
     * @return 생성된 액세스 토큰 문자열
     * @explain 주어진 사용자 정보를 바탕으로 만료 기간이 설정된 액세스 토큰을 생성하여 반환합니다.
     */
    public String createAccessToken(String email, Long userId, String nickname, String authorities, Long coupleId) {
        return createToken(email, accessTokenExpirationTime, TYPE_ACCESS, userId, nickname, authorities, coupleId);
    }

    /**
     * 리프레시 토큰 생성 메서드
     *
     * @param email 사용자 이메일
     * @param userId 사용자 ID
     * @param nickname 사용자 닉네임
     * @return 생성된 리프레시 토큰 문자열
     * @explain 주어진 사용자 정보를 바탕으로 만료 기간이 설정된 리프레시 토큰을 생성하여 반환합니다.
     */
    public String createRefreshToken(String email, Long userId, String nickname, Long coupleId) {
        return createToken(email, refreshTokenExpirationTime, TYPE_REFRESH, userId, nickname, "", coupleId);
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

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * JWT 토큰에서 로그인 정보를 가져오는 메서드
     *
     * @param token JWT 토큰
     * @return LoginInfo 객체 (userId와 nickname이 포함됨)
     */
    public LoginInfo getLoginInfoFromToken(String token) {
        Claims claims = extractClaims(token);

        Long userId = claims.get("userId", Long.class); // Claims에서 userId 추출
        String nickname = claims.get("nickname", String.class); // Claims에서 nickname 추출
        String authorities = claims.get(AUTHORITIES_KEY, String.class); // Claims에서 authorities 추출
        Long coupleId = claims.get("coupleId", Long.class);


        // 문자열로 된 권한을 Authority 열거형으로 변환
        Authority authority = Authority.valueOf(authorities);

        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setUserId(userId);
        loginInfo.setNickname(nickname);
        loginInfo.setAuthorities(authority); // LoginInfo에 권한 정보 추가
        loginInfo.setCoupleId(coupleId);

        return loginInfo;
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
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급하는 메서드
     *
     * @param refreshToken 유효한 리프레시 토큰
     * @return 새로 발급된 액세스 토큰 문자열
     * @throws InvalidAccessTokenException 리프레시 토큰이 유효하지 않거나 만료된 경우 예외 발생
     */
    public String reissueAccessToken(String refreshToken) {
        if (validateToken(refreshToken) && isRefreshToken(refreshToken) && !isTokenExpired(refreshToken)) {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            String email = claims.getSubject();
            Long userId = claims.get("userId", Long.class);
            String nickname = claims.get("nickname", String.class);
            String authorities = claims.get(AUTHORITIES_KEY, String.class);
            Long coupleId = claims.get("coupleId", Long.class);

            return createAccessToken(email, userId, nickname, authorities, coupleId);
        } else {
            throw new InvalidAccessTokenException();
        }
    }
}
