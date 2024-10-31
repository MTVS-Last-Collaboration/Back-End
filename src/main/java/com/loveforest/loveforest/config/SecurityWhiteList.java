package com.loveforest.loveforest.config;

import org.springframework.stereotype.Component;

@Component
public class SecurityWhiteList {
    private static final String[] WHITE_LIST = {
        "/api/auth/**",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
            "/actuator/**"
    };

    public String[] getWhiteList() {
        return WHITE_LIST;
    }
}
