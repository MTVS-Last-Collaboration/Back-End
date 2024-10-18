package com.loveforest.loveforest.domain.auth.jwt;

import com.loveforest.loveforest.domain.auth.jwt.exception.InvalidAccessTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (token != null) {
            try {
                jwtTokenProvider.validateAccessToken(token);
                String email = jwtTokenProvider.getEmailFromToken(token);
                var userDetails = userDetailsService.loadUserByUsername(email);

                var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("인증 성공 - 이메일: {}", maskEmail(email));
            } catch (InvalidAccessTokenException e) {
                log.warn("유효하지 않은 액세스 토큰: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "액세스 토큰이 유효하지 않거나 만료되었습니다.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String maskEmail(String email) {
        String[] parts = email.split("@");
        return parts[0].substring(0, Math.min(3, parts[0].length())) + "***@" + parts[1];
    }
}
