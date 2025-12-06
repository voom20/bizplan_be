# REQ-FUNC-AUTH-BE-001: 사용자 인증 API (회원가입/로그인)

## 1. 개요
- **목표**: 이메일/비밀번호 기반 사용자 인증 시스템을 구현하여 사용자별 프로젝트 격리 및 보안 접근 제어를 제공한다.
- **범위**:
  - `POST /api/v1/auth/signup`: 신규 사용자 회원가입
  - `POST /api/v1/auth/login`: JWT 토큰 발급 (로그인)
  - `POST /api/v1/auth/logout`: 토큰 무효화 (로그아웃)
  - `POST /api/v1/auth/refresh`: 토큰 갱신
- **Out of Scope**: 소셜 로그인(OAuth2), 2FA(다중 인증), 비밀번호 찾기/재설정 (Post-MVP).

## 2. 상세 요구사항

### 2.1 회원가입 (Signup)
- **요청**: 이메일, 비밀번호, 이름(선택)
- **검증**:
  - 이메일 형식 유효성 검사
  - 비밀번호 최소 8자, 대소문자+숫자+특수문자 조합 권장
  - 중복 이메일 체크
- **응답**: 생성된 사용자 ID, 이메일, 가입일시

### 2.2 로그인 (Login)
- **요청**: 이메일, 비밀번호
- **검증**: 이메일 존재 여부, 비밀번호 일치 확인
- **응답**: Access Token (JWT), Refresh Token, 만료 시간
- **토큰 구조**: 
  - Access Token: 짧은 유효기간 (15분~1시간)
  - Refresh Token: 긴 유효기간 (7일~30일)

### 2.3 로그아웃 (Logout)
- **요청**: Authorization 헤더의 Access Token
- **처리**: Refresh Token 무효화 (블랙리스트 또는 DB 삭제)
- **응답**: 성공/실패 메시지

### 2.4 토큰 갱신 (Refresh)
- **요청**: Refresh Token
- **검증**: Refresh Token 유효성 및 만료 여부 확인
- **응답**: 새로운 Access Token

## 3. API 명세

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| POST | `/api/v1/auth/signup` | 회원가입 | No |
| POST | `/api/v1/auth/login` | 로그인 | No |
| POST | `/api/v1/auth/logout` | 로그아웃 | Yes |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 | No (Refresh Token 필요) |

## 4. 에러 코드

| 코드 | 메시지 | 설명 |
|------|--------|------|
| AUTH_001 | 이미 존재하는 이메일입니다 | 중복 이메일로 회원가입 시도 |
| AUTH_002 | 이메일 또는 비밀번호가 올바르지 않습니다 | 로그인 실패 |
| AUTH_003 | 토큰이 만료되었습니다 | Access Token 만료 |
| AUTH_004 | 유효하지 않은 토큰입니다 | 토큰 검증 실패 |
| AUTH_005 | 비밀번호 형식이 올바르지 않습니다 | 비밀번호 정책 위반 |

---

```yaml
task_id: "REQ-FUNC-AUTH-BE-001"
title: "사용자 인증 API (회원가입/로그인/로그아웃)"
summary: >
  이메일/비밀번호 기반 JWT 인증 시스템을 구현하여 
  사용자 회원가입, 로그인, 로그아웃, 토큰 갱신 기능을 제공한다.
type: "functional"
epic: "EPIC_AUTH"
req_ids: ["REQ-NF-006", "REQ-NF-007", "REQ-NF-008"]
component: ["backend.auth", "backend.security"]
agent_profile: ["backend"]

inputs:
  signup:
    fields:
      - name: "email"
        type: "string"
        required: true
        validation: "email format"
      - name: "password"
        type: "string"
        required: true
        validation: "min 8 chars"
      - name: "name"
        type: "string"
        required: false
  login:
    fields:
      - name: "email"
        type: "string"
        required: true
      - name: "password"
        type: "string"
        required: true

outputs:
  signup:
    http_status: 201
    body_fields: ["user_id", "email", "created_at"]
  login:
    http_status: 200
    body_fields: ["access_token", "refresh_token", "token_type", "expires_in"]

steps_hint:
  - "User 엔티티 정의 (id, email, password_hash, name, created_at, updated_at)"
  - "UserRepository: JPA Repository 인터페이스"
  - "AuthService: 회원가입, 로그인, 토큰 발급 로직"
  - "JwtTokenProvider: JWT 토큰 생성/검증 유틸리티"
  - "AuthController: 인증 엔드포인트 구현"
  - "PasswordEncoder: BCrypt 해싱 설정"
  - "RefreshToken 엔티티 및 저장 로직 (선택: Redis 또는 DB)"

preconditions:
  - "MySQL DB가 실행 중이어야 한다."
  - "Spring Security 의존성이 추가되어야 한다."
  - "JWT 라이브러리(jjwt)가 추가되어야 한다."

postconditions:
  - "회원가입 시 DB에 사용자 정보가 저장된다."
  - "비밀번호는 BCrypt로 해싱되어 저장된다."
  - "로그인 성공 시 JWT 토큰이 발급된다."

dependencies: ["REQ-NF-006-SEC-001"]

acceptance_criteria:
  - given: "유효한 이메일과 비밀번호로 회원가입을 요청하면"
    when: "POST /api/v1/auth/signup 호출"
    then: "201 Created와 함께 사용자 정보가 반환되고 DB에 저장된다"
  - given: "이미 존재하는 이메일로 회원가입을 요청하면"
    when: "POST /api/v1/auth/signup 호출"
    then: "409 Conflict와 AUTH_001 에러가 반환된다"
  - given: "올바른 이메일/비밀번호로 로그인하면"
    when: "POST /api/v1/auth/login 호출"
    then: "200 OK와 함께 JWT 토큰이 발급된다"
  - given: "잘못된 비밀번호로 로그인하면"
    when: "POST /api/v1/auth/login 호출"
    then: "401 Unauthorized와 AUTH_002 에러가 반환된다"
```

