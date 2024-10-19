package com.loveforest.loveforest.domain.auth.jwt;

import com.loveforest.loveforest.domain.auth.jwt.exception.InvalidAccessTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = jwtTokenProvider.resolveToken((HttpServletRequest) request, "accessToken");
        String requestURI = ((HttpServletRequest) request).getRequestURI();

        if (token != null && jwtTokenProvider.validateToken(token)) {
            if (!requestURI.equals("/api/auth/refresh") && !requestURI.equals("/api/auth/logout")) {
                try {
                    jwtTokenProvider.validateAccessToken(token);
                    Authentication auth = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.info("{}님이 인증되었습니다. uri: {}", auth.getName(), requestURI);
                } catch (InvalidAccessTokenException e) {
                    log.warn("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
                }
            }
        } else {
            log.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
        }

        chain.doFilter(request, response);
    }
}
