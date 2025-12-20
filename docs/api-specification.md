# API Specification

BizPlan Backend REST API 명세서입니다.

## Overview

| 항목 | 값 |
|------|-----|
| Base URL | `http://localhost:8080` |
| API Version | v1 |
| Content-Type | `application/json` |
| 인증 | JWT Bearer Token |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI Spec | `http://localhost:8080/api-docs` |

---

## 목차

1. [Authentication API](#1-authentication-api) - 인증
2. [Users API](#2-users-api) - 사용자 관리
3. [Projects API](#3-projects-api) - 프로젝트 관리
4. [Wizard API](#4-wizard-api) - Wizard 답변 관리
5. [Financials API](#5-financials-api) - 재무 추정
6. [Business Plan Documents API](#6-business-plan-documents-api) - 사업계획서 생성
7. [BizPlan Sections API](#7-bizplan-sections-api) - AI 섹션 생성/관리
8. [PMF API](#8-pmf-api) - Product-Market Fit 진단
9. [Export API](#9-export-api) - 문서 내보내기
10. [Actuator API](#10-actuator-api) - 모니터링

---

## 1. Authentication API

사용자 인증 및 토큰 관리

### 1.1 회원가입

신규 사용자를 등록합니다.

```
POST /auth/signup
```

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "name": "홍길동"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | string | ✅ | 이메일 (로그인 ID) |
| password | string | ✅ | 비밀번호 (최소 8자) |
| name | string | ❌ | 사용자 이름 |

**Response 201**

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "name": "홍길동",
  "role": "USER",
  "createdAt": "2025-12-06T10:30:00"
}
```

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 400 | 유효하지 않은 입력 (이메일 형식, 비밀번호 길이 등) |
| 409 | 이미 존재하는 이메일 |

---

### 1.2 로그인

사용자 인증 및 JWT 토큰 발급

```
POST /auth/login
```

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response 200**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "name": "홍길동",
    "role": "USER"
  }
}
```

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 401 | 인증 실패 (잘못된 이메일/비밀번호) |

---

### 1.3 토큰 갱신

Refresh Token으로 새 Access Token 발급

```
POST /auth/refresh
```

**Request Body**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response 200**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 401 | 유효하지 않거나 만료된 Refresh Token |

---

### 1.4 로그아웃

현재 세션 종료 (토큰 무효화)

```
POST /auth/logout
```

**Headers**

```
Authorization: Bearer {accessToken}
```

**Response 200**

```json
{
  "message": "로그아웃 되었습니다"
}
```

---

## 2. Users API

사용자 프로필 관리

### 2.1 내 프로필 조회

현재 로그인한 사용자의 프로필 정보를 조회합니다.

```
GET /users/me
```

**Headers**

```
Authorization: Bearer {accessToken}
```

**Response 200**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "name": "홍길동",
  "companyName": "스타트업 주식회사",
  "phone": "010-1234-5678",
  "role": "일반 사용자",
  "lastLoginAt": "2025-12-06T10:30:00",
  "createdAt": "2025-12-01T09:00:00"
}
```

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 401 | 인증되지 않은 요청 |
| 404 | 사용자를 찾을 수 없음 |

---

### 2.2 프로필 수정

사용자 프로필 정보를 수정합니다.

```
PUT /users/me
```

**Headers**

```
Authorization: Bearer {accessToken}
```

**Request Body**

```json
{
  "name": "홍길동",
  "companyName": "새로운 회사명",
  "phone": "010-9999-8888"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| name | string | ❌ | 이름 (최대 100자) |
| companyName | string | ❌ | 회사명 (최대 200자) |
| phone | string | ❌ | 연락처 (최대 20자) |

**Response 200**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "name": "홍길동",
  "companyName": "새로운 회사명",
  "phone": "010-9999-8888",
  "role": "일반 사용자",
  "lastLoginAt": "2025-12-06T10:30:00",
  "createdAt": "2025-12-01T09:00:00"
}
```

---

### 2.3 비밀번호 변경

사용자 비밀번호를 변경합니다.

```
PUT /users/me/password
```

**Headers**

```
Authorization: Bearer {accessToken}
```

**Request Body**

```json
{
  "currentPassword": "currentPassword123",
  "newPassword": "newSecurePassword456",
  "newPasswordConfirm": "newSecurePassword456"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| currentPassword | string | ✅ | 현재 비밀번호 |
| newPassword | string | ✅ | 새 비밀번호 (최소 8자) |
| newPasswordConfirm | string | ✅ | 새 비밀번호 확인 |

**Response 200**

```json
{
  "message": "비밀번호가 변경되었습니다"
}
```

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 400 | 현재 비밀번호 불일치 또는 새 비밀번호 확인 불일치 |

---

### 2.4 회원 탈퇴

사용자 계정을 삭제합니다 (소프트 삭제).

```
DELETE /users/me
```

**Headers**

```
Authorization: Bearer {accessToken}
```

**Request Body**

```json
{
  "password": "currentPassword123"
}
```

**Response 200**

```json
{
  "message": "회원 탈퇴가 완료되었습니다"
}
```

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 400 | 비밀번호 불일치 |

---

## 3. Projects API

사업계획서 프로젝트 생성 및 관리

### 3.1 템플릿 목록 조회

사용 가능한 사업계획서 템플릿 목록을 반환합니다.

```
GET /projects/templates
```

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| category | string | ❌ | 카테고리 필터 (`government`, `bank`, `investor`) |

**Response 200**

```json
[
  {
    "code": "KSTARTUP_2025",
    "displayName": "예비창업패키지 2025",
    "description": "K-Startup 예비창업패키지 지원용 사업계획서",
    "category": "government"
  },
  {
    "code": "KSTARTUP_EARLY_2025",
    "displayName": "초기창업패키지 2025",
    "description": "K-Startup 초기창업패키지 지원용 사업계획서",
    "category": "government"
  },
  {
    "code": "BANK_LOAN_2025",
    "displayName": "은행 대출용 2025",
    "description": "시중은행 사업자대출 심사용 사업계획서",
    "category": "bank"
  },
  {
    "code": "INVESTOR_PITCH_2025",
    "displayName": "투자유치용 2025",
    "description": "VC/엔젤 투자유치용 사업계획서",
    "category": "investor"
  }
]
```

---

### 3.2 프로젝트 생성

새 사업계획서 프로젝트를 생성합니다.

```
POST /projects
```

**Request Body**

```json
{
  "templateCode": "KSTARTUP_2025",
  "title": "내 첫 사업계획서"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| templateCode | string | ✅ | 템플릿 코드 |
| title | string | ❌ | 프로젝트 제목 (최대 255자) |

**Response 201**

```json
{
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "templateCode": "KSTARTUP_2025",
  "title": "내 첫 사업계획서",
  "status": "DRAFT",
  "wizardAnswers": {},
  "createdAt": "2025-12-06T10:30:00",
  "updatedAt": "2025-12-06T10:30:00"
}
```

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 400 | 유효하지 않은 템플릿 코드 |

---

### 3.3 프로젝트 조회

특정 프로젝트의 상세 정보를 조회합니다.

```
GET /projects/{projectId}
```

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| projectId | string (UUID) | 프로젝트 ID |

**Response 200**

```json
{
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "templateCode": "KSTARTUP_2025",
  "title": "내 첫 사업계획서",
  "status": "IN_PROGRESS",
  "wizardAnswers": {
    "problem_definition": {
      "problem": "기존 시장의 문제점..."
    }
  },
  "createdAt": "2025-12-06T10:30:00",
  "updatedAt": "2025-12-06T11:00:00"
}
```

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 404 | 프로젝트를 찾을 수 없음 |

---

### 3.4 내 프로젝트 목록

현재 사용자의 프로젝트 목록을 조회합니다.

```
GET /projects
```

**Response 200**

```json
[
  {
    "projectId": "550e8400-e29b-41d4-a716-446655440000",
    "templateCode": "KSTARTUP_2025",
    "title": "내 첫 사업계획서",
    "status": "IN_PROGRESS",
    "wizardAnswers": {},
    "createdAt": "2025-12-06T10:30:00",
    "updatedAt": "2025-12-06T11:00:00"
  }
]
```

---

### 3.5 프로젝트 수정

프로젝트 제목 및 상태를 수정합니다.

```
PATCH /projects/{projectId}
```

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| projectId | string (UUID) | 프로젝트 ID |

**Request Body**

```json
{
  "title": "수정된 프로젝트 제목",
  "status": "IN_PROGRESS"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | string | ❌ | 프로젝트 제목 (최대 255자) |
| status | string | ❌ | 프로젝트 상태 (ENUM) |

**Response 200**

```json
{
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "templateCode": "KSTARTUP_2025",
  "title": "수정된 프로젝트 제목",
  "status": "IN_PROGRESS",
  "wizardAnswers": {},
  "createdAt": "2025-12-06T10:30:00",
  "updatedAt": "2025-12-06T12:00:00"
}
```

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 403 | 접근 권한 없음 |
| 404 | 프로젝트를 찾을 수 없음 |

---

### 3.6 프로젝트 삭제

프로젝트를 삭제합니다. 관련된 모든 데이터가 함께 삭제됩니다.

```
DELETE /projects/{projectId}
```

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| projectId | string (UUID) | 프로젝트 ID |

**Response 204**

No Content

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 403 | 접근 권한 없음 |
| 404 | 프로젝트를 찾을 수 없음 |

---

## 4. Wizard API

Wizard 단계별 답변 저장 및 조회

### 4.1 단계 정의 조회

템플릿별 Wizard 단계 및 질문 정의를 조회합니다.

```
GET /projects/{projectId}/wizard/steps
```

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| projectId | string (UUID) | 프로젝트 ID |

**Response 200**

```json
{
  "templateCode": "PRE_STARTUP",
  "totalSteps": 5,
  "steps": [
    {
      "id": 1,
      "title": "사업 아이디어",
      "description": "사업 아이디어를 설명해주세요",
      "questions": [
        {
          "id": "business-name",
          "type": "text",
          "label": "사업명",
          "placeholder": "사업명을 입력하세요",
          "required": true,
          "maxLength": 100
        },
        {
          "id": "business-description",
          "type": "textarea",
          "label": "사업 설명",
          "placeholder": "사업 아이디어를 설명해주세요",
          "required": true,
          "maxLength": 1000
        }
      ]
    }
  ]
}
```

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 400 | 잘못된 템플릿 코드 |
| 404 | 프로젝트를 찾을 수 없음 |

---

### 4.2 답변 저장

특정 단계의 답변을 저장합니다. 기존 답변과 병합됩니다.

```
POST /projects/{projectId}/wizard/steps
```

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| projectId | string (UUID) | 프로젝트 ID |

**Request Body**

```json
{
  "stepId": "problem_definition",
  "answers": {
    "problem": "기존 시장에서 창업자들이 겪는 어려움...",
    "target_customer": "예비창업자, 소상공인"
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| stepId | string | ✅ | 단계 ID |
| answers | object | ✅ | 답변 데이터 (Key-Value) |

**Response 200**

```json
{
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "answers": {
    "problem_definition": {
      "problem": "기존 시장에서 창업자들이 겪는 어려움...",
      "target_customer": "예비창업자, 소상공인"
    }
  },
  "completedSteps": 1,
  "totalSteps": 10
}
```

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 400 | 잘못된 요청 |
| 404 | 프로젝트를 찾을 수 없음 |

---

### 4.2 전체 답변 조회

프로젝트의 모든 Wizard 답변을 조회합니다.

```
GET /projects/{projectId}/wizard/answers
```

**Response 200**

```json
{
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "answers": {
    "problem_definition": { "problem": "..." },
    "solution": { "solution": "..." },
    "market_analysis": { "tam": 1000000000 }
  },
  "completedSteps": 3,
  "totalSteps": 10
}
```

---

### 4.3 단계별 답변 조회

특정 단계의 답변만 조회합니다.

```
GET /projects/{projectId}/wizard/steps/{stepId}
```

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| projectId | string (UUID) | 프로젝트 ID |
| stepId | string | 단계 ID |

**Response 200**

```json
{
  "problem": "기존 시장에서 창업자들이 겪는 어려움...",
  "target_customer": "예비창업자, 소상공인"
}
```

---

## 5. Financials API

재무 추정 및 유닛 이코노믹스 계산

### 5.1 재무 데이터 조회

저장된 재무 추정 결과를 조회합니다.

```
GET /projects/{projectId}/financials
```

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| projectId | string (UUID) | 프로젝트 ID |

**Response 200**

`5.2 재무 추정 생성`의 응답과 동일

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 404 | 재무 데이터 없음 |

---

### 5.2 재무 추정 생성 (프로젝트 연동)

프로젝트에 연동된 재무 추정을 생성합니다.

```
POST /projects/{projectId}/financials/generate
```

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| projectId | string (UUID) | 프로젝트 ID |

**Request Body**

```json
{
  "initialCapital": 100000000,
  "averageRevenuePerUser": 50000,
  "monthlyMarketingBudget": 5000000,
  "customerAcquisitionCost": 30000,
  "monthlyChurnRate": 0.05,
  "monthlyFixedCosts": 10000000,
  "variableCostRate": 0.3,
  "initialCustomers": 0,
  "projectionMonths": 36
}
```

| 필드 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| initialCapital | number | ✅ | - | 초기 자본금 (원) |
| averageRevenuePerUser | number | ✅ | - | 월 평균 객단가/ARPU (원) |
| monthlyMarketingBudget | number | ✅ | - | 월간 마케팅 예산 (원) |
| customerAcquisitionCost | number | ✅ | - | 고객 획득 비용/CAC (원) |
| monthlyChurnRate | number | ✅ | - | 월간 이탈률 (0.0~1.0) |
| monthlyFixedCosts | number | ✅ | - | 월 고정비 (원) |
| variableCostRate | number | ❌ | 0.3 | 변동비율 (0.0~1.0) |
| initialCustomers | number | ❌ | 0 | 초기 고객 수 |
| projectionMonths | number | ❌ | 36 | 추정 기간 (개월) |

**Response 200**

```json
{
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "monthlyPL": [
    {
      "month": 1,
      "year": 1,
      "activeCustomers": 166,
      "newCustomers": 166,
      "churnedCustomers": 0,
      "revenue": 8300000,
      "variableCosts": 2490000,
      "fixedCosts": 10000000,
      "marketingCosts": 5000000,
      "operatingProfit": -9190000,
      "cumulativeCash": 90810000
    }
  ],
  "yearlySummary": [
    {
      "year": 1,
      "totalRevenue": 150000000,
      "totalCosts": 200000000,
      "operatingProfit": -50000000,
      "endCustomers": 1500,
      "growthRate": 0
    }
  ],
  "unitEconomics": {
    "cac": 30000,
    "ltv": 1000000,
    "ltvCacRatio": 33.33,
    "breakEvenMonth": 18,
    "breakEvenCustomers": 500,
    "monthlyChurnRate": 0.05,
    "averageCustomerLifespan": 20,
    "averageNetNewCustomers": 100
  }
}
```

---

### 5.3 재무 추정 미리보기 (독립형)

프로젝트 연동 없이 재무 추정 결과를 미리 확인합니다.

```
POST /financials/preview
```

**Request Body**

`5.2 재무 추정 생성`과 동일

**Response 200**

`5.2 재무 추정 생성`과 동일 (`projectId`가 `"preview"`로 설정됨)

---

### 5.4 재무 가정값 저장

재무 시뮬레이션 가정값만 저장합니다 (시뮬레이션 재실행 없음).

```
PUT /projects/{projectId}/financials/assumptions
```

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| projectId | string (UUID) | 프로젝트 ID |

**Request Body**

```json
{
  "initialCapital": 100000000,
  "averageRevenuePerUser": 50000,
  "monthlyMarketingBudget": 5000000,
  "customerAcquisitionCost": 30000,
  "monthlyChurnRate": 0.05,
  "monthlyFixedCosts": 10000000,
  "variableCostRate": 0.3
}
```

**Response 200**

```json
{
  "message": "저장되었습니다.",
  "updatedAt": "2025-12-20T10:30:00"
}
```

---

## 6. Business Plan Documents API

AI 기반 사업계획서 생성 및 관리

### 6.1 사업계획서 전체 생성

Wizard 답변을 기반으로 AI가 사업계획서를 생성합니다.

```
POST /projects/{projectId}/documents/business-plan/generate
```

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| projectId | string (UUID) | 프로젝트 ID |

**Response 200**

```json
{
  "documentId": "660e8400-e29b-41d4-a716-446655440001",
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "version": 1,
  "status": "DRAFT",
  "sections": {
    "executive_summary": "본 사업계획서는...",
    "problem_definition": "현재 시장에서...",
    "solution": "당사의 솔루션은...",
    "market_analysis": "TAM/SAM/SOM 분석...",
    "business_model": "수익 모델은...",
    "competitive_analysis": "경쟁사 대비...",
    "marketing_strategy": "마케팅 전략은...",
    "team": "핵심 팀원 구성...",
    "financial_plan": "3개년 재무 계획...",
    "milestones": "주요 마일스톤..."
  },
  "totalWordCount": 15000,
  "generationTimeMs": 45000,
  "createdAt": "2025-12-06T12:00:00",
  "updatedAt": "2025-12-06T12:00:00"
}
```

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 400 | Wizard 답변 미완료 |
| 500 | AI 엔진 오류 |

---

### 6.2 섹션 재생성

기존 문서의 특정 섹션만 다시 생성합니다.

```
POST /projects/{projectId}/documents/{documentId}/sections/{sectionType}/regenerate
```

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| projectId | string (UUID) | 프로젝트 ID |
| documentId | string (UUID) | 문서 ID |
| sectionType | string | 섹션 타입 |

**섹션 타입**

| 값 | 설명 |
|-----|------|
| executive_summary | 요약 |
| problem_definition | 문제 정의 |
| solution | 솔루션 |
| market_analysis | 시장 분석 |
| business_model | 비즈니스 모델 |
| competitive_analysis | 경쟁 분석 |
| marketing_strategy | 마케팅 전략 |
| team | 팀 소개 |
| financial_plan | 재무 계획 |
| milestones | 마일스톤 |

**Response 200**

`6.1 사업계획서 전체 생성`과 동일

---

### 6.3 최신 문서 조회

프로젝트의 가장 최근 문서를 조회합니다.

```
GET /projects/{projectId}/documents/business-plan/latest
```

**Response 200**

`6.1 사업계획서 전체 생성`과 동일

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 404 | 문서 없음 |

---

### 6.4 특정 문서 조회

문서 ID로 특정 문서를 조회합니다.

```
GET /projects/{projectId}/documents/{documentId}
```

**Response 200**

`6.1 사업계획서 전체 생성`과 동일

---

### 6.5 문서 버전 목록

프로젝트의 모든 문서 버전을 조회합니다.

```
GET /projects/{projectId}/documents/business-plan/versions
```

**Response 200**

```json
[
  {
    "documentId": "...",
    "projectId": "...",
    "version": 2,
    "status": "DRAFT",
    "sections": {...},
    "createdAt": "2025-12-06T14:00:00"
  },
  {
    "documentId": "...",
    "projectId": "...",
    "version": 1,
    "status": "FINALIZED",
    "sections": {...},
    "createdAt": "2025-12-06T12:00:00"
  }
]
```

---

## 7. BizPlan Sections API

AI를 통한 사업계획서 섹션 생성 및 관리

### 7.1 AI 섹션 생성

AI Engine을 통해 특정 섹션을 생성합니다.

```
POST /projects/{projectId}/bizplan/sections/{sectionType}/generate
```

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| projectId | string (UUID) | 프로젝트 ID |
| sectionType | string | 섹션 타입 |

**Request Body**

```json
{
  "mode": "easy",
  "additionalInstructions": "기술적인 내용을 더 강조해주세요"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| mode | string | ❌ | 생성 모드 (`easy` / `expert`) |
| additionalInstructions | string | ❌ | AI에게 전달할 추가 지시사항 |

**Response 200**

```json
{
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "sectionType": "executive_summary",
  "title": "사업개요",
  "content": "AI가 생성한 섹션 내용...",
  "wordCount": 500,
  "modelUsed": "gemini-1.5-pro",
  "generationTimeMs": 3500,
  "suggestions": ["개선 제안 1", "개선 제안 2"],
  "consistencyWarnings": [],
  "updatedAt": "2025-12-20T10:30:00"
}
```

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 400 | 잘못된 섹션 타입 |
| 404 | 프로젝트를 찾을 수 없음 |
| 500 | AI 엔진 오류 |

---

### 7.2 섹션 목록 조회

프로젝트의 모든 섹션 목록을 조회합니다.

```
GET /projects/{projectId}/bizplan/sections
```

**Response 200**

```json
{
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "sections": [
    {
      "sectionType": "executive_summary",
      "title": "사업개요",
      "wordCount": 500,
      "updatedAt": "2025-12-20T10:30:00"
    }
  ],
  "totalSections": 10,
  "completedSections": 3
}
```

---

### 7.3 섹션 조회

특정 섹션의 내용을 조회합니다.

```
GET /projects/{projectId}/bizplan/sections/{sectionType}
```

**Response 200**

`7.1 AI 섹션 생성`의 응답과 동일 (suggestions, consistencyWarnings 제외)

---

### 7.4 섹션 수정

섹션의 제목과 내용을 수동으로 수정합니다.

```
PUT /projects/{projectId}/bizplan/sections/{sectionType}
```

**Request Body**

```json
{
  "title": "수정된 제목",
  "content": "수정된 내용..."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | string | ✅ | 섹션 제목 (최대 255자) |
| content | string | ✅ | 섹션 내용 |

**Response 200**

`7.1 AI 섹션 생성`의 응답과 동일

---

### 7.5 섹션 삭제

특정 섹션을 삭제합니다.

```
DELETE /projects/{projectId}/bizplan/sections/{sectionType}
```

**Response 204**

No Content

---

### 7.6 섹션 타입 목록 조회

지원되는 모든 섹션 타입 목록을 조회합니다.

```
GET /bizplan/section-types
```

**Response 200**

```json
{
  "sectionTypes": [
    {
      "type": "executive_summary",
      "name": "사업개요",
      "description": "사업의 핵심 내용을 요약합니다."
    },
    {
      "type": "problem_definition",
      "name": "문제 정의",
      "description": "해결하고자 하는 문제를 정의합니다."
    },
    {
      "type": "solution",
      "name": "솔루션",
      "description": "제안하는 솔루션을 설명합니다."
    }
  ]
}
```

---

## 8. PMF API

Product-Market Fit 진단

### 8.1 PMF 설문 질문 조회

PMF 진단용 설문 질문 목록을 반환합니다.

```
GET /pmf/questions
```

**Response 200**

```json
{
  "questions": [
    {
      "id": "pmf-1",
      "question": "만약 이 제품을 더 이상 사용할 수 없게 된다면 어떻게 느끼시겠습니까?",
      "type": "radio",
      "required": true,
      "options": [
        { "value": 1, "label": "매우 실망할 것이다" },
        { "value": 2, "label": "다소 실망할 것이다" },
        { "value": 3, "label": "실망하지 않을 것이다" }
      ]
    }
  ]
}
```

---

### 8.2 PMF 평가 기준 조회

PMF 진단에 사용되는 평가 기준 목록을 조회합니다.

```
GET /pmf/criteria
```

**Response 200**

```json
{
  "criteria": [
    {
      "category": "market_fit",
      "name": "시장 적합성",
      "weight": 0.3,
      "description": "목표 시장에 대한 적합성 평가"
    },
    {
      "category": "problem_solution",
      "name": "문제-솔루션 적합성",
      "weight": 0.25,
      "description": "문제 해결 능력 평가"
    }
  ]
}
```

---

### 8.3 PMF 설문 결과 제출

PMF 설문 답변을 제출하고 분석 결과를 반환합니다.

```
POST /projects/{projectId}/pmf/submit
```

**Request Body**

```json
{
  "answers": [
    { "questionId": "pmf-1", "value": 1 },
    { "questionId": "pmf-2", "value": [1, 2, 3] },
    { "questionId": "pmf-5", "value": 9 }
  ]
}
```

**Response 200**

```json
{
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "score": 45,
  "level": "excellent",
  "risks": [
    {
      "id": "risk-1",
      "title": "PMF 미달성",
      "description": "현재 제품이 시장의 핵심 니즈를 충족하지 못하고 있습니다.",
      "level": "high"
    }
  ],
  "recommendations": [
    {
      "id": "rec-1",
      "title": "고객 인터뷰 진행",
      "description": "핵심 고객층과 심층 인터뷰를 통해 제품 개선점을 파악하세요.",
      "priority": "high"
    }
  ],
  "answers": [...],
  "createdAt": "2025-12-20T10:30:00"
}
```

---

### 8.4 PMF 리포트 조회

저장된 PMF 분석 결과를 조회합니다.

```
GET /projects/{projectId}/pmf/report
```

**Response 200**

`8.3 PMF 설문 결과 제출`의 응답과 동일

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 404 | PMF 리포트 없음 |

---

### 8.5 AI PMF 진단 실행

사업계획서 내용과 재무 데이터를 AI로 분석하여 PMF를 진단합니다.

```
POST /projects/{projectId}/pmf/ai-diagnose
```

**Request Body**

```json
{
  "includeFinancialData": true
}
```

| 필드 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| includeFinancialData | boolean | ❌ | true | 재무 데이터 포함 여부 |

**Response 200**

```json
{
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "overallScore": 75,
  "scoreGrade": "good",
  "categoryScores": {
    "market_fit": 80,
    "problem_solution": 70,
    "revenue_model": 75
  },
  "risks": [
    {
      "category": "market",
      "title": "시장 규모 불확실",
      "description": "목표 시장의 규모 검증이 필요합니다.",
      "level": "medium",
      "recommendation": "추가 시장 조사를 진행하세요.",
      "relatedSections": ["market_analysis"]
    }
  ],
  "strengths": ["명확한 문제 정의", "차별화된 솔루션"],
  "summary": "전반적으로 양호한 PMF 수준입니다...",
  "priorityActions": ["시장 규모 검증", "고객 피드백 수집"],
  "modelUsed": "gemini-1.5-pro",
  "analysisTimeMs": 3500,
  "analyzedAt": "2025-12-20T10:30:00"
}
```

**PMF 점수 등급**

| Grade | 점수 범위 | 설명 |
|-------|----------|------|
| excellent | 80-100 | 뛰어남 |
| good | 60-79 | 좋음 |
| fair | 40-59 | 보통 |
| poor | 20-39 | 미흡 |
| critical | 0-19 | 심각 |

---

### 8.6 AI PMF 진단 결과 조회

저장된 AI PMF 진단 결과를 조회합니다.

```
GET /projects/{projectId}/pmf/ai-diagnose
```

**Response 200**

`8.5 AI PMF 진단 실행`의 응답과 동일

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 404 | 진단 결과 없음 |

---

## 9. Export API

사업계획서 내보내기 (PDF/HTML)

### 7.1 사업계획서 내보내기

최신 버전의 문서를 다운로드합니다.

```
GET /projects/{projectId}/export
```

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| format | string | ❌ | pdf | 출력 형식 (`pdf`, `html`) |

**Response 200**

- Content-Type: `application/pdf` 또는 `text/html`
- Content-Disposition: `attachment; filename="bizplan_2025-12-06.pdf"`
- Body: 파일 바이너리

**Errors**

| 상태코드 | 설명 |
|----------|------|
| 400 | 지원하지 않는 형식 |
| 404 | 문서 없음 |

---

### 7.2 특정 버전 내보내기

지정된 버전의 문서를 다운로드합니다.

```
GET /projects/{projectId}/export/versions/{version}
```

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| projectId | string (UUID) | 프로젝트 ID |
| version | int | 문서 버전 |

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| format | string | pdf | 출력 형식 |

---

### 7.3 지원 형식 목록

사용 가능한 내보내기 형식을 반환합니다.

```
GET /projects/{projectId}/export/formats
```

**Response 200**

```json
{
  "formats": ["pdf", "html"],
  "defaultFormat": "pdf",
  "note": "HWP 형식은 향후 지원 예정입니다."
}
```

---

## 10. Actuator API

애플리케이션 모니터링

### 10.1 Health Check

```
GET /actuator/health
```

**Response 200**

```json
{
  "status": "UP"
}
```

---

### 10.2 Prometheus Metrics

```
GET /actuator/prometheus
```

**Response 200**

```
# HELP jvm_memory_used_bytes Used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap"} 1.2345678E8
...
```

---

## 공통 에러 응답

모든 API는 에러 발생 시 다음 형식으로 응답합니다.

```json
{
  "timestamp": "2025-12-06T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "유효하지 않은 템플릿 코드입니다: INVALID_CODE",
  "path": "/projects"
}
```

---

## ENUM 정의

### ProjectStatus

| 값 | 설명 |
|----|------|
| DRAFT | 초안 작성 중 |
| IN_PROGRESS | Wizard 진행 중 |
| SUBMITTED | 제출 완료 |
| APPROVED | 승인됨 |
| REJECTED | 거절됨 |

### DocumentStatus

| 값 | 설명 |
|----|------|
| GENERATING | AI 생성 중 |
| DRAFT | 초안 (편집 가능) |
| REVIEWING | 검토 중 |
| FINALIZED | 최종 확정 |
| FAILED | 생성 실패 |

### TemplateCode

| 값 | 카테고리 | 설명 |
|----|----------|------|
| KSTARTUP_2025 | government | 예비창업패키지 2025 |
| KSTARTUP_EARLY_2025 | government | 초기창업패키지 2025 |
| BANK_LOAN_2025 | bank | 은행 대출용 2025 |
| INVESTOR_PITCH_2025 | investor | 투자유치용 2025 |

### ExportFormat

| 값 | Content-Type | 확장자 |
|----|--------------|--------|
| PDF | application/pdf | .pdf |
| HTML | text/html | .html |

---

## Rate Limits

MVP에서는 Rate Limit을 적용하지 않습니다.

향후 다음과 같이 적용 예정:
- 일반 API: 100 requests/minute
- 문서 생성 API: 10 requests/minute (AI 비용 고려)

---

## ENUM 정의 (추가)

### UserRole

| 값 | 권한 | 설명 |
|----|------|------|
| USER | ROLE_USER | 일반 사용자 (프로젝트 5개 제한) |
| PREMIUM | ROLE_PREMIUM | 프리미엄 사용자 (무제한) |
| ADMIN | ROLE_ADMIN | 관리자 (모든 권한) |

### UserStatus

| 값 | 설명 |
|----|------|
| ACTIVE | 활성 상태 |
| SUSPENDED | 정지 상태 |
| DELETED | 삭제 상태 (소프트 삭제) |

---

## Changelog

| 버전 | 날짜 | 변경사항 |
|------|------|----------|
| 1.3.0 | 2025-12-20 | BizPlan Sections API 추가, PMF API 추가, AI Engine 연동 |
| 1.2.0 | 2025-12-20 | Projects PATCH/DELETE 추가, Wizard 단계 정의 API 추가, Financials 조회/저장 API 추가 |
| 1.1.0 | 2025-12-06 | Authentication/Users API 추가, JWT 인증 적용 |
| 1.0.0 | 2025-12-06 | 초기 버전 |

