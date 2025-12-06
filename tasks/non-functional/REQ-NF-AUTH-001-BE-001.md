# REQ-NF-AUTH-001-BE-001: Spring Security 및 JWT 인증 설정

## 1. 개요
- **목표**: Spring Security를 활용한 JWT 기반 인증/인가 시스템의 인프라 및 보안 설정을 구성한다.
- **범위**:
  - Spring Security 필터 체인 구성
  - JWT 토큰 검증 필터 구현
  - CORS 설정
  - 권한 기반 엔드포인트 접근 제어
  - 비밀번호 인코더 설정
- **Out of Scope**: OAuth2/소셜 로그인, Spring Security ACL (Post-MVP).

## 2. 상세 요구사항

### 2.1 Spring Security 설정
- **인증 방식**: Stateless (세션 미사용)
- **필터 체인**:
  1. CorsFilter
  2. JwtAuthenticationFilter (커스텀)
  3. ExceptionTranslationFilter
  4. FilterSecurityInterceptor

### 2.2 JWT 설정
- **알고리즘**: HS256 또는 RS256
- **토큰 구성**:
  ```json
  {
    "sub": "user-id",
    "email": "user@example.com",
    "roles": ["USER"],
    "iat": 1700000000,
    "exp": 1700003600
  }
  ```
- **Secret Key**: 환경변수 `JWT_SECRET_KEY`로 관리
- **만료 시간**: 환경변수로 설정 가능
  - Access Token: `JWT_ACCESS_EXPIRATION` (기본: 3600초)
  - Refresh Token: `JWT_REFRESH_EXPIRATION` (기본: 604800초)

### 2.3 엔드포인트 접근 제어
| 패턴 | 접근 권한 |
|------|-----------|
| `/api/v1/auth/**` | 모두 허용 (permitAll) |
| `/api/v1/projects/**` | 인증 필요 (authenticated) |
| `/api/v1/wizard/**` | 인증 필요 (authenticated) |
| `/api/v1/financial/**` | 인증 필요 (authenticated) |
| `/api/v1/export/**` | 인증 필요 (authenticated) |
| `/api/v1/users/**` | 인증 필요 (authenticated) |
| `/api/v1/admin/**` | ADMIN 권한 필요 (hasRole('ADMIN')) |
| `/actuator/health` | 모두 허용 |
| `/swagger-ui/**` | 개발 환경만 허용 |

### 2.4 CORS 설정
```yaml
# 허용 Origin (환경별 설정)
development:
  - http://localhost:3000
  - http://localhost:5173
production:
  - https://app.bizplan.co.kr
  - https://www.bizplan.co.kr
```

### 2.5 예외 처리
- **AuthenticationEntryPoint**: 401 Unauthorized 응답 (미인증)
- **AccessDeniedHandler**: 403 Forbidden 응답 (권한 부족)

## 3. 구성 클래스

```
src/main/java/com/vibe/bizplan/bizplan_be/
├── config/
│   └── security/
│       ├── SecurityConfig.java           # Spring Security 메인 설정
│       ├── JwtAuthenticationFilter.java  # JWT 검증 필터
│       ├── JwtTokenProvider.java         # JWT 생성/검증 유틸리티
│       ├── CustomAuthenticationEntryPoint.java
│       ├── CustomAccessDeniedHandler.java
│       └── CorsConfig.java               # CORS 설정
├── domain/
│   └── entity/
│       └── User.java                     # UserDetails 구현
```

## 4. 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `JWT_SECRET_KEY` | JWT 서명 키 (최소 256bit) | 필수 |
| `JWT_ACCESS_EXPIRATION` | Access Token 만료 시간(초) | 3600 |
| `JWT_REFRESH_EXPIRATION` | Refresh Token 만료 시간(초) | 604800 |
| `CORS_ALLOWED_ORIGINS` | 허용된 Origin 목록 (쉼표 구분) | localhost |

---

```yaml
task_id: "REQ-NF-AUTH-001-BE-001"
title: "Spring Security JWT 인증/인가 설정"
summary: >
  Spring Security와 JWT를 활용한 stateless 인증 시스템을 구성하고
  엔드포인트별 접근 제어 정책을 적용한다.
type: "non_functional"
epic: "EPIC_SECURITY"
req_ids: ["REQ-NF-006", "REQ-NF-007", "REQ-NF-008"]
component: ["backend.security"]
agent_profile: ["backend", "infra"]

category: "security"
labels: ["security:jwt", "security:spring-security", "infrastructure"]

requirements:
  description: >
    모든 보호된 API 엔드포인트는 유효한 JWT 토큰을 요구하며,
    사용자 역할에 따른 접근 제어가 적용되어야 한다.
  kpis:
    - "인증/인가 관련 보안 취약점 0건"
    - "토큰 위변조 시도 100% 차단"

steps_hint:
  - "build.gradle: Spring Security, jjwt 의존성 추가"
  - "JwtTokenProvider: 토큰 생성, 검증, 파싱 로직 구현"
  - "JwtAuthenticationFilter: OncePerRequestFilter 확장"
  - "SecurityConfig: @EnableWebSecurity, SecurityFilterChain 빈 설정"
  - "CustomAuthenticationEntryPoint: 401 응답 핸들러"
  - "CustomAccessDeniedHandler: 403 응답 핸들러"
  - "application.yml: JWT 설정 프로퍼티 추가"
  - "User 엔티티: UserDetails 인터페이스 구현"

preconditions:
  - "Spring Boot 3.x 프로젝트가 구성되어 있어야 한다."

postconditions:
  - "유효한 JWT 없이 보호된 API 호출 시 401 응답"
  - "권한 부족 시 403 응답"
  - "유효한 JWT로 API 호출 시 정상 처리"

dependencies: []

test_scenarios:
  - description: "토큰 없이 보호된 API 호출"
    expected: "401 Unauthorized"
  - description: "만료된 토큰으로 API 호출"
    expected: "401 Unauthorized, 토큰 만료 메시지"
  - description: "위변조된 토큰으로 API 호출"
    expected: "401 Unauthorized, 유효하지 않은 토큰 메시지"
  - description: "USER 권한으로 ADMIN API 호출"
    expected: "403 Forbidden"
  - description: "유효한 토큰으로 보호된 API 호출"
    expected: "정상 응답"
```

