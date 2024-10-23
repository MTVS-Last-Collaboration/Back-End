package com.loveforest.loveforest.domain.auth.jwt;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.auth.jwt.exception.InvalidAccessTokenException;
import com.loveforest.loveforest.domain.user.enums.Authority;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = jwtTokenProvider.resolveToken((HttpServletRequest) request, "accessToken");

        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                // getLoginInfoFromToken을 사용하여 사용자 정보 및 권한 정보 추출
                LoginInfo loginInfo = jwtTokenProvider.getLoginInfoFromToken(token);

                // 권한 정보 생성 (Authority 열거형을 기반으로 GrantedAuthority 생성)
                List<GrantedAuthority> authorities = Arrays.stream(loginInfo.getAuthorities().name().split(","))
                        .map(role -> (GrantedAuthority) () -> role)
                        .collect(Collectors.toList());

                // 인증 객체 생성
                Authentication auth = new UsernamePasswordAuthenticationToken(loginInfo, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                log.warn("JWT 토큰 인증 실패: {}", e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }
}
