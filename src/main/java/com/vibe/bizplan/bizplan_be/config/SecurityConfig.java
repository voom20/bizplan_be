package com.vibe.bizplan.bizplan_be.config;

import com.vibe.bizplan.bizplan_be.infrastructure.security.CustomAccessDeniedHandler;
import com.vibe.bizplan.bizplan_be.infrastructure.security.CustomAuthenticationEntryPoint;
import com.vibe.bizplan.bizplan_be.infrastructure.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정.
 * JWT 기반 stateless 인증 및 엔드포인트별 접근 제어를 설정한다.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    /**
     * BCrypt 기반 PasswordEncoder 빈.
     * 사용자 비밀번호를 안전하게 해시화하는 데 사용한다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager 빈.
     * 인증 처리를 담당한다.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Security Filter Chain 설정.
     * JWT 인증, CORS, 엔드포인트 접근 제어를 구성한다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (REST API는 stateless이므로 불필요)
            .csrf(AbstractHttpConfigurer::disable)
            
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 세션 관리: Stateless (JWT 토큰 기반 인증)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // H2 Console 허용을 위한 Frame Options 설정
            .headers(headers -> 
                headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            
            // 예외 처리 핸들러 등록
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            
            // 엔드포인트 접근 제어
            .authorizeHttpRequests(auth -> auth
                // 인증 API - 모두 허용
                .requestMatchers("/api/v1/auth/**").permitAll()
                
                // Swagger UI 및 API 문서 - 모두 허용
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                
                // H2 Console (개발용) - 모두 허용
                .requestMatchers("/h2-console/**").permitAll()
                
                // 헬스체크/모니터링 - 모두 허용
                .requestMatchers("/actuator/**").permitAll()
                
                // Admin API - ADMIN 권한 필요
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                
                // 그 외 API - 인증 필요 (MVP에서는 일단 permitAll 유지)
                // TODO: MVP 이후 authenticated()로 변경
                .anyRequest().permitAll()
            )
            
            // JWT 인증 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * CORS 설정.
     * 허용된 Origin, Method, Header를 정의한다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용 Origin (환경별로 분리 필요)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5173"
        ));
        
        // 허용 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // 허용 헤더
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"
        ));
        
        // 노출 헤더 (클라이언트에서 접근 가능한 응답 헤더)
        configuration.setExposedHeaders(List.of("Authorization"));
        
        // 자격 증명 허용
        configuration.setAllowCredentials(true);
        
        // Pre-flight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
