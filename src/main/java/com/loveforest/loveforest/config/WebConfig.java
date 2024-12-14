package com.loveforest.loveforest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /s3/** 경로로 오는 요청을 프로젝트의 s3 폴더에서 찾도록 설정
        registry.addResourceHandler("/s3/**")
                .addResourceLocations("file:s3/")    // 여기서 s3는 실제 폴더명
                .setCachePeriod(3600)                // 캐시 1시간
                .resourceChain(true);                // 리소스 체인 활성화
    }
}
