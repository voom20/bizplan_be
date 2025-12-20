package com.vibe.bizplan.bizplan_be.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * OpenAPI (Swagger) 문서 설정.
 * API 문서화를 위한 메타데이터, 보안 스키마, 태그 그룹을 정의한다.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    /**
     * OpenAPI 문서 설정 빈.
     * JWT Bearer 인증, API 메타데이터, 태그 그룹을 설정한다.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // JWT Bearer 인증 스키마 정의
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                // API 기본 정보
                .info(apiInfo())
                
                // 서버 정보
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("https://api.bizplan.vibe.com")
                                .description("운영 서버 (예정)")
                ))
                
                // 보안 스키마 컴포넌트 등록
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT 액세스 토큰을 입력하세요. (예: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...)")
                        )
                )
                
                // 전역 보안 요구사항 (인증이 필요한 API에 자동 적용)
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                
                // API 태그 그룹 정의 (정렬 순서)
                .tags(List.of(
                        new Tag().name("Auth").description("🔐 인증 API - 회원가입, 로그인, 토큰 갱신"),
                        new Tag().name("Users").description("👤 사용자 관리 API - 프로필 조회/수정, 비밀번호 변경"),
                        new Tag().name("Projects").description("📁 프로젝트 관리 API - 프로젝트 생성, 조회, 템플릿"),
                        new Tag().name("Wizard").description("🧙 위자드 API - 단계별 답변 저장/조회"),
                        new Tag().name("Business Plan Documents").description("📄 사업계획서 API - 문서 생성, 섹션 재생성"),
                        new Tag().name("Document Export").description("📥 내보내기 API - PDF/HTML 다운로드"),
                        new Tag().name("Financials").description("💰 재무 추정 API - 손익계산, 유닛 이코노믹스"),
                        new Tag().name("Financials Preview").description("🔮 재무 미리보기 API - 프로젝트 미연동 계산")
                ));
    }

    /**
     * API 기본 정보 설정.
     */
    private Info apiInfo() {
        return new Info()
                .title("BizPlan API")
                .description("""
                        ## 🚀 AI Co-Pilot for First-time Founders
                        
                        창업자를 위한 AI 사업계획서 자동 생성 플랫폼의 백엔드 API입니다.
                        
                        ### 주요 기능
                        - **Wizard 입력**: 단계별 질문 응답으로 사업 정보 수집
                        - **AI 문서 생성**: LLM 기반 사업계획서 초안 자동 생성
                        - **재무 추정**: 핵심 변수 기반 3년 손익/현금흐름 계산
                        - **문서 내보내기**: PDF/HTML 형식 다운로드
                        
                        ### 인증 방식
                        - JWT Bearer Token 인증
                        - Access Token (1시간) + Refresh Token (7일)
                        
                        ### 에러 응답 형식
                        ```json
                        {
                          "timestamp": "2025-01-01T12:00:00",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "에러 메시지",
                          "path": "/api/v1/..."
                        }
                        ```
                        """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("BizPlan Team")
                        .email("support@bizplan.vibe.com")
                        .url("https://bizplan.vibe.com"))
                .license(new License()
                        .name("Private License")
                        .url("https://bizplan.vibe.com/license"));
    }
}
