package com.loveforest.loveforest.domain.auth.jwt;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtHandshakeInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // ServerHttpRequest를 HttpServletRequest로 변환
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

            // HTTP 헤더에서 JWT 추출 및 검증
            String token = jwtTokenProvider.resolveToken(servletRequest, "accessToken"); // HttpServletRequest로 전달
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // getLoginInfoFromToken을 사용하여 LoginInfo 추출
                LoginInfo loginInfo = jwtTokenProvider.getLoginInfoFromToken(token);

                // WebSocket 세션에 필요한 사용자 정보를 저장
                attributes.put("userId", loginInfo.getUserId());
                attributes.put("nickname", loginInfo.getNickname());
                attributes.put("authorities", loginInfo.getAuthorities().name());

                return true;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
        // 이후 처리 필요 시 구현
    }
}