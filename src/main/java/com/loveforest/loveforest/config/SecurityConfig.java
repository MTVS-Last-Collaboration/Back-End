package com.loveforest.loveforest.config;

import com.loveforest.loveforest.domain.auth.jwt.JwtTokenFilter;
import com.loveforest.loveforest.domain.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CorsConfig corsConfig;
    private final SecurityWhiteList securityWhiteList;

    /**
     * SecurityFilterChain 빈을 정의하여 Spring Security의 보안 필터 체인을 구성하는 메서드
     *
     * @param http HttpSecurity 객체를 통해 보안 설정을 적용
     * @param mvc MvcRequestMatcher.Builder 객체를 통해 엔드포인트 매칭 처리
     * @return 설정된 SecurityFilterChain 객체
     * @throws Exception 설정 도중 발생할 수 있는 예외 처리
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource())) // CORS 설정 추가
                .csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화
                .sessionManagement((sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안 함
                .authorizeHttpRequests((request) -> request
                        .requestMatchers(mvc.pattern("/api/users/**")).permitAll()  // "/api/users/**"는 인증 필요
                        .requestMatchers(mvc.pattern("/api/chat/**")).permitAll()  // "/api/users/**"는 인증 필요
                        .requestMatchers(this.createMvcRequestMatcherForWhiteList(mvc)).permitAll() // 화이트리스트는 인증 없이 접근 가능
                        .anyRequest().authenticated()) // 그 외 모든 요청은 인증 필요
                .addFilterBefore(new JwtTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);  // JWT 필터 추가

        return http.build();
    }


    /**
     * PasswordEncoder 빈을 생성하는 메서드
     *
     * @return BCryptPasswordEncoder 객체를 반환하여 비밀번호 암호화에 사용
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager 빈을 정의하는 메서드
     *
     * @param authenticationConfiguration AuthenticationConfiguration 객체로부터 인증 매니저 생성
     * @return 생성된 AuthenticationManager 객체
     * @throws Exception 예외 처리
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * MvcRequestMatcher.Builder 빈을 생성하는 메서드
     *
     * @param introspector HandlerMappingIntrospector 객체를 사용하여 MVC 경로 매칭을 처리
     * @return MvcRequestMatcher.Builder 객체
     */
    @Bean
    public MvcRequestMatcher.Builder mvcRequestMatcherBuilder(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    /**
     * 화이트리스트에 있는 엔드포인트를 MvcRequestMatcher 배열로 생성하는 메서드
     *
     * @param mvc MvcRequestMatcher.Builder 객체
     * @return MvcRequestMatcher 배열
     */
    private MvcRequestMatcher[] createMvcRequestMatcherForWhiteList(MvcRequestMatcher.Builder mvc) {
        return Stream.of(securityWhiteList.getWhiteList()).map(mvc::pattern).toArray(MvcRequestMatcher[]::new);
    }


}

