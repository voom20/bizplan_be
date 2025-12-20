# EPIC0-FE-006: 회원가입 및 로그인 인증 UI PoC

## 1. 개요
- **목표**: 사용자가 이메일/비밀번호로 회원가입 및 로그인할 수 있는 인증 화면을 구현하고, JWT 토큰 기반 인증 흐름을 연동한다.
- **범위**:
  - 회원가입 페이지 (이메일, 비밀번호, 비밀번호 확인)
  - 로그인 페이지 (이메일, 비밀번호)
  - JWT 토큰 저장 및 관리 (localStorage 또는 httpOnly Cookie)
  - 인증 상태에 따른 라우트 보호 (Protected Route)
  - 자동 토큰 갱신 (Refresh Token)
- **Out of Scope**: 소셜 로그인(OAuth), 비밀번호 찾기/재설정.

## 2. 상세 요구사항
- **회원가입**: 이메일 형식 검증, 비밀번호 8자 이상 + 숫자/특수문자 포함 검증, 비밀번호 확인 일치 검증.
- **로그인**: 이메일/비밀번호 입력 후 API 호출, 실패 시 에러 메시지 표시.
- **토큰 관리**: 로그인 성공 시 Access Token과 Refresh Token 저장, API 요청 시 Authorization 헤더에 자동 추가.
- **인증 상태**: `useAuthStore`를 통해 로그인 상태를 전역 관리하고, 비로그인 시 보호된 페이지 접근 차단.
- **자동 로그아웃**: Access Token 만료 시 Refresh Token으로 갱신 시도, 실패 시 로그인 페이지로 리다이렉트.

## 3. 기술 스택 및 도구
- React + Vite + TypeScript
- React Hook Form + Zod (폼 검증)
- Zustand (인증 상태 관리)
- Axios Interceptor (토큰 자동 첨부 및 갱신)

---

```yaml
task_id: "EPIC0-FE-006"
title: "회원가입 및 로그인 인증 UI PoC 구현"
summary: >
  이메일/비밀번호 기반 회원가입, 로그인 화면을 구현하고,
  JWT 토큰 인증 흐름과 Protected Route를 적용한다.
type: "functional"

epic: "EPIC_0_FE_PROTOTYPE"
req_ids: ["REQ-FUNC-AUTH", "REQ-NF-AUTH-001"]
agent_profile: ["frontend"]

parallelizable: true
estimated_effort: "M"
priority: "Must"

inputs:
  description: "사용자 인증 정보"
  fields:
    - name: "email"
      type: "string"
      example: "user@example.com"
    - name: "password"
      type: "string"
      example: "SecurePass123!"

outputs:
  description: "JWT 토큰 및 인증 상태"
  success:
    tokens: { accessToken: "string", refreshToken: "string" }
    ui_state: "Authenticated User Redirect to Dashboard"

steps_hint:
  - "SignupPage 컴포넌트 구현 (폼 검증 포함)"
  - "LoginPage 컴포넌트 구현"
  - "useAuthStore 구현 (login, logout, refreshToken 액션)"
  - "Axios Interceptor 설정 (토큰 자동 첨부, 401 처리)"
  - "ProtectedRoute 컴포넌트 구현"
  - "AuthController API 연동 (/api/v1/auth/*)"

preconditions:
  - "React+Vite 기본 프로젝트가 셋업되어 있어야 한다."
  - "백엔드 AuthController API가 구현되어 있어야 한다."

postconditions:
  - "회원가입 성공 시 로그인 페이지로 이동한다."
  - "로그인 성공 시 대시보드(프로젝트 목록)로 이동한다."
  - "비로그인 상태에서 보호된 페이지 접근 시 로그인으로 리다이렉트된다."
  - "Access Token 만료 시 자동으로 갱신을 시도한다."

dependencies: []
```
