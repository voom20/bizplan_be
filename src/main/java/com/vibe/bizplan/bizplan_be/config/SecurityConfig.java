package com.vibe.bizplan.bizplan_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정.
 * MVP 단계에서는 인증 없이 API를 허용하되, 향후 확장을 위한 기본 구조를 설정한다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * BCrypt 기반 PasswordEncoder 빈.
     * 사용자 비밀번호를 안전하게 해시화하는 데 사용한다.
     * 
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security Filter Chain 설정.
     * MVP 단계: 모든 API 허용, CSRF 비활성화 (REST API용).
     * 
     * @param http HttpSecurity 설정 객체
     * @return 설정된 SecurityFilterChain
     * @throws Exception 설정 오류 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (REST API는 stateless이므로 불필요)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 세션 관리: Stateless (JWT 등 토큰 기반 인증을 위해)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // H2 Console 허용을 위한 Frame Options 설정
            .headers(headers -> 
                headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            
            // MVP: 모든 요청 허용 (향후 인증 적용 예정)
            .authorizeHttpRequests(auth -> auth
                // Swagger UI 및 API 문서
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                // H2 Console (개발용)
                .requestMatchers("/h2-console/**").permitAll()
                // 헬스체크
                .requestMatchers("/actuator/**").permitAll()
                // MVP: 모든 API 허용
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}

