# REQ-FUNC-USER-BE-001: 사용자 관리 API

## 1. 개요
- **목표**: 사용자 프로필 조회, 수정, 탈퇴 등 사용자 계정 관리 기능을 제공한다.
- **범위**:
  - `GET /api/v1/users/me`: 내 프로필 조회
  - `PATCH /api/v1/users/me`: 프로필 수정 (이름, 회사명 등)
  - `PUT /api/v1/users/me/password`: 비밀번호 변경
  - `DELETE /api/v1/users/me`: 회원 탈퇴
- **Out of Scope**: 관리자용 사용자 목록 조회/관리 (Admin API는 별도 태스크).

## 2. 상세 요구사항

### 2.1 프로필 조회 (GET /users/me)
- **인증**: 필수 (Access Token)
- **응답**: 사용자 ID, 이메일, 이름, 회사명, 가입일, 마지막 로그인 시간

### 2.2 프로필 수정 (PATCH /users/me)
- **인증**: 필수 (Access Token)
- **수정 가능 필드**: 이름, 회사명, 전화번호(선택)
- **수정 불가 필드**: 이메일 (변경 시 별도 인증 플로우 필요 - Post-MVP)

### 2.3 비밀번호 변경 (PUT /users/me/password)
- **인증**: 필수 (Access Token)
- **요청**: 현재 비밀번호, 새 비밀번호, 새 비밀번호 확인
- **검증**:
  - 현재 비밀번호 일치 확인
  - 새 비밀번호 정책 검증
  - 새 비밀번호 == 새 비밀번호 확인
- **처리**: 비밀번호 변경 후 기존 토큰 무효화 (선택적)

### 2.4 회원 탈퇴 (DELETE /users/me)
- **인증**: 필수 (Access Token)
- **요청**: 비밀번호 확인 (보안)
- **처리**:
  - 소프트 삭제 (deleted_at 타임스탬프 설정) 권장
  - 관련 프로젝트, 문서 데이터 처리 정책 필요
  - 탈퇴 후 30일간 복구 가능 (선택)

## 3. User 엔티티 스키마

```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
    email VARCHAR(255) NOT NULL UNIQUE COMMENT '로그인 이메일',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt 해시된 비밀번호',
    name VARCHAR(100) COMMENT '사용자 이름',
    company_name VARCHAR(200) COMMENT '회사/프로젝트명',
    phone VARCHAR(20) COMMENT '연락처 (선택)',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '권한: USER, ADMIN',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '상태: ACTIVE, SUSPENDED, DELETED',
    last_login_at DATETIME COMMENT '마지막 로그인 시간',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME COMMENT '소프트 삭제 시간',
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## 4. API 명세

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|-----------|
| GET | `/api/v1/users/me` | 내 프로필 조회 | Yes |
| PATCH | `/api/v1/users/me` | 프로필 수정 | Yes |
| PUT | `/api/v1/users/me/password` | 비밀번호 변경 | Yes |
| DELETE | `/api/v1/users/me` | 회원 탈퇴 | Yes |

## 5. 에러 코드

| 코드 | 메시지 | 설명 |
|------|--------|------|
| USER_001 | 사용자를 찾을 수 없습니다 | 존재하지 않는 사용자 |
| USER_002 | 현재 비밀번호가 일치하지 않습니다 | 비밀번호 변경 시 검증 실패 |
| USER_003 | 새 비밀번호가 일치하지 않습니다 | 새 비밀번호 확인 불일치 |
| USER_004 | 이미 탈퇴된 계정입니다 | 탈퇴 처리된 계정 접근 |

---

```yaml
task_id: "REQ-FUNC-USER-BE-001"
title: "사용자 관리 API (프로필 조회/수정/탈퇴)"
summary: >
  인증된 사용자가 자신의 프로필을 조회, 수정하고 
  비밀번호 변경 및 회원 탈퇴를 수행할 수 있는 API를 구현한다.
type: "functional"
epic: "EPIC_AUTH"
req_ids: ["REQ-NF-008"]
component: ["backend.user"]
agent_profile: ["backend"]

inputs:
  profile_update:
    fields:
      - name: "name"
        type: "string"
        required: false
      - name: "company_name"
        type: "string"
        required: false
      - name: "phone"
        type: "string"
        required: false
  password_change:
    fields:
      - name: "current_password"
        type: "string"
        required: true
      - name: "new_password"
        type: "string"
        required: true
      - name: "new_password_confirm"
        type: "string"
        required: true

outputs:
  profile:
    http_status: 200
    body_fields: ["id", "email", "name", "company_name", "created_at", "last_login_at"]

steps_hint:
  - "User 엔티티 확장 (name, company_name, phone, status, deleted_at 필드 추가)"
  - "UserService: 프로필 조회/수정/삭제 로직"
  - "UserController: 사용자 관리 엔드포인트 구현"
  - "UserMapper: Entity <-> DTO 변환"
  - "Flyway 마이그레이션: users 테이블 생성"

preconditions:
  - "REQ-FUNC-AUTH-BE-001이 완료되어야 한다."
  - "JWT 인증이 구현되어 있어야 한다."

postconditions:
  - "인증된 사용자만 자신의 프로필에 접근할 수 있다."
  - "비밀번호 변경 시 새 비밀번호가 BCrypt로 해싱되어 저장된다."
  - "회원 탈퇴 시 소프트 삭제가 적용된다."

dependencies: ["REQ-FUNC-AUTH-BE-001"]

acceptance_criteria:
  - given: "인증된 사용자가 프로필 조회를 요청하면"
    when: "GET /api/v1/users/me 호출"
    then: "200 OK와 함께 사용자 프로필 정보가 반환된다"
  - given: "인증되지 않은 요청이 프로필 조회를 시도하면"
    when: "Authorization 헤더 없이 GET /api/v1/users/me 호출"
    then: "401 Unauthorized가 반환된다"
  - given: "올바른 현재 비밀번호와 새 비밀번호로 변경을 요청하면"
    when: "PUT /api/v1/users/me/password 호출"
    then: "200 OK와 함께 비밀번호가 변경된다"
  - given: "잘못된 현재 비밀번호로 변경을 요청하면"
    when: "PUT /api/v1/users/me/password 호출"
    then: "400 Bad Request와 USER_002 에러가 반환된다"
```

