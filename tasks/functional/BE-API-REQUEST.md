# 백엔드 API 추가 개발 요청서

> 📅 작성일: 2025-12-20  
> 👤 요청자: Frontend Team  
> 📌 우선순위: 🔴 High / 🟠 Medium / 🟡 Low

---

## 요약

프론트엔드에서 필요하지만 현재 백엔드 API에 없는 엔드포인트 목록입니다.

| # | API | 우선순위 | 카테고리 |
|---|-----|---------|---------|
| 1 | 프로젝트 수정 | 🔴 High | Projects |
| 2 | 프로젝트 삭제 | 🔴 High | Projects |
| 3 | 위저드 단계 정의 조회 | 🟠 Medium | Wizard |
| 4 | 재무 데이터 조회 | 🟠 Medium | Financials |
| 5 | 재무 데이터 저장 | 🟠 Medium | Financials |
| 6 | PMF 설문 질문 조회 | 🟡 Low | PMF |
| 7 | PMF 설문 결과 제출 | 🟡 Low | PMF |
| 8 | PMF 리포트 조회 | 🟡 Low | PMF |

---

## 1. 프로젝트 수정 API 🔴

### 요청 사항
프로젝트 제목 및 메타데이터 수정 기능

### 엔드포인트
```
PATCH /projects/{projectId}
🔒 Authorization Required
```

### Request Body
```json
{
  "title": "수정된 프로젝트명",
  "status": "IN_PROGRESS"
}
```

### Response (200)
```json
{
  "projectId": "project-uuid",
  "templateCode": "pre-startup",
  "title": "수정된 프로젝트명",
  "status": "IN_PROGRESS",
  "updatedAt": "2025-01-01T12:00:00"
}
```

### 사용처
- 프로젝트 설정 페이지
- 프로젝트 이름 변경 기능

---

## 2. 프로젝트 삭제 API 🔴

### 요청 사항
사용자가 자신의 프로젝트를 삭제할 수 있는 기능

### 엔드포인트
```
DELETE /projects/{projectId}
🔒 Authorization Required
```

### Response (204)
No Content

### 에러 응답
| Status | Description |
|--------|-------------|
| 403 | 권한 없음 (본인 프로젝트가 아님) |
| 404 | 프로젝트 없음 |

### 사용처
- 프로젝트 목록 페이지 (삭제 버튼)
- 프로젝트 설정 페이지

---

## 3. 위저드 단계 정의 조회 API 🟠

### 요청 사항
템플릿별 위저드 단계 및 질문 정의 조회

### 배경
현재 프론트엔드에서 `mockData.ts`의 하드코딩된 `wizardSteps`를 사용 중.
템플릿마다 다른 질문 세트가 필요할 수 있음.

### 엔드포인트
```
GET /projects/{projectId}/wizard/steps
🔒 Authorization Required
```

또는 템플릿 기반:
```
GET /templates/{templateCode}/wizard/steps
```

### Response (200)
```json
{
  "templateCode": "pre-startup",
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
    },
    {
      "id": 2,
      "title": "목표 시장",
      "description": "목표 시장을 정의해주세요",
      "questions": [...]
    }
  ]
}
```

### Question Type 정의
| Type | Description |
|------|-------------|
| `text` | 한 줄 텍스트 입력 |
| `textarea` | 여러 줄 텍스트 입력 |
| `number` | 숫자 입력 |
| `select` | 단일 선택 (드롭다운) |
| `multiselect` | 복수 선택 |
| `date` | 날짜 선택 |
| `radio` | 라디오 버튼 |

### 사용처
- WizardStep 컴포넌트
- QuestionForm 컴포넌트

---

## 4. 재무 데이터 조회 API 🟠

### 요청 사항
저장된 재무 시뮬레이션 데이터 조회

### 배경
현재 `/financials/generate`만 있고, 이전에 생성한 재무 데이터를 조회하는 API가 없음.
재접속 시 마지막 시뮬레이션 결과를 표시해야 함.

### 엔드포인트
```
GET /projects/{projectId}/financials
🔒 Authorization Required
```

### Response (200)
```json
{
  "projectId": "project-uuid",
  "assumptions": {
    "initialCapital": 50000000,
    "averageRevenuePerUser": 30000,
    "monthlyMarketingBudget": 5000000,
    "customerAcquisitionCost": 10000,
    "monthlyChurnRate": 0.05,
    "monthlyFixedCosts": 10000000
  },
  "monthlyPL": [...],
  "yearlySummary": [...],
  "unitEconomics": {
    "ltv": 600000,
    "cac": 10000,
    "ltvCacRatio": 60,
    "paybackPeriodMonths": 3
  },
  "createdAt": "2025-01-01T12:00:00",
  "updatedAt": "2025-01-01T12:00:00"
}
```

### Response (404)
재무 데이터가 없는 경우
```json
{
  "message": "재무 데이터가 없습니다."
}
```

### 사용처
- FinancialSimulation 컴포넌트 (초기 로드)
- 사업계획서 재무 섹션

---

## 5. 재무 데이터 저장 API 🟠

### 요청 사항
재무 가정값만 저장 (시뮬레이션 재실행 없이)

### 배경
현재 `/financials/generate`는 항상 시뮬레이션을 다시 실행함.
가정값만 저장하고 싶을 때 사용.

### 엔드포인트
```
PUT /projects/{projectId}/financials/assumptions
🔒 Authorization Required
```

### Request Body
```json
{
  "initialCapital": 50000000,
  "averageRevenuePerUser": 30000,
  "monthlyMarketingBudget": 5000000,
  "customerAcquisitionCost": 10000,
  "monthlyChurnRate": 0.05,
  "monthlyFixedCosts": 10000000,
  "variableCostRate": 0.3,
  "initialCustomers": 100,
  "projectionMonths": 36
}
```

### Response (200)
```json
{
  "message": "저장되었습니다.",
  "updatedAt": "2025-01-01T12:00:00"
}
```

### 사용처
- 자동 저장 기능 (Auto-save)
- 임시 저장 버튼

---

## 6. PMF 설문 질문 조회 API 🟡

### 요청 사항
PMF(Product-Market Fit) 진단용 설문 질문 목록 조회

### 엔드포인트
```
GET /pmf/questions
```

또는 템플릿별:
```
GET /templates/{templateCode}/pmf/questions
```

### Response (200)
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
        { "value": 3, "label": "실망하지 않을 것이다" },
        { "value": 4, "label": "해당 없음" }
      ]
    },
    {
      "id": "pmf-2",
      "question": "이 제품의 주요 혜택은 무엇입니까?",
      "type": "multiselect",
      "required": true,
      "options": [
        { "value": 1, "label": "시간 절약" },
        { "value": 2, "label": "비용 절감" },
        { "value": 3, "label": "품질 향상" },
        { "value": 4, "label": "기타" }
      ]
    }
  ]
}
```

### 사용처
- PMFSurvey 컴포넌트
- 위저드 5단계

---

## 7. PMF 설문 결과 제출 API 🟡

### 요청 사항
PMF 설문 답변 제출 및 분석 결과 생성

### 엔드포인트
```
POST /projects/{projectId}/pmf/submit
🔒 Authorization Required
```

### Request Body
```json
{
  "answers": [
    { "questionId": "pmf-1", "value": 1 },
    { "questionId": "pmf-2", "value": [1, 2] },
    { "questionId": "pmf-3", "value": 1 }
  ]
}
```

### Response (200)
```json
{
  "projectId": "project-uuid",
  "score": 42,
  "level": "excellent",
  "risks": [
    {
      "id": "risk-1",
      "title": "시장 검증 필요",
      "description": "더 많은 고객 피드백이 필요합니다.",
      "severity": "medium"
    }
  ],
  "recommendations": [
    {
      "id": "rec-1",
      "title": "고객 인터뷰 진행",
      "description": "핵심 고객층과 심층 인터뷰를 진행하세요.",
      "priority": "high"
    }
  ],
  "createdAt": "2025-01-01T12:00:00"
}
```

### PMF Level 정의
| Level | Score 범위 | 설명 |
|-------|-----------|------|
| `excellent` | 40% 이상 | PMF 달성 |
| `high` | 30-39% | 거의 달성 |
| `medium` | 20-29% | 개선 필요 |
| `low` | 20% 미만 | PMF 미달성 |

### 사용처
- PMFSurvey 컴포넌트 (결과 제출)

---

## 8. PMF 리포트 조회 API 🟡

### 요청 사항
저장된 PMF 분석 결과 조회

### 엔드포인트
```
GET /projects/{projectId}/pmf/report
🔒 Authorization Required
```

### Response (200)
```json
{
  "projectId": "project-uuid",
  "score": 42,
  "level": "excellent",
  "risks": [...],
  "recommendations": [...],
  "answers": [
    { "questionId": "pmf-1", "value": 1 },
    { "questionId": "pmf-2", "value": [1, 2] }
  ],
  "createdAt": "2025-01-01T12:00:00"
}
```

### Response (404)
PMF 리포트가 없는 경우
```json
{
  "message": "PMF 리포트가 없습니다."
}
```

### 사용처
- PMFSurvey 컴포넌트 (이전 결과 로드)
- 사업계획서 PMF 섹션

---

## 추가 고려사항

### 1. 버전 관리
현재 API prefix가 `/api/v1`과 없는 것이 혼재됨.
- Auth/Users: `/api/v1/auth/*`, `/api/v1/users/*`
- Projects/Wizard: `/projects/*`

**권장**: 일관된 버전 prefix 사용
```
/api/v1/projects/{projectId}/...
```

### 2. 에러 응답 형식 통일
```json
{
  "timestamp": "2025-01-01T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "에러 메시지",
  "path": "/api/v1/..."
}
```

### 3. 페이지네이션 (프로젝트 목록)
현재 `/projects`가 배열을 직접 반환하는데, 프로젝트가 많아지면 페이지네이션 필요:
```json
{
  "data": [...],
  "meta": {
    "page": 1,
    "limit": 20,
    "total": 100,
    "totalPages": 5
  }
}
```

---

## 우선순위별 개발 일정 제안

| 단계 | API | 예상 공수 |
|------|-----|----------|
| **Phase 1** | 프로젝트 수정/삭제 | 0.5일 |
| **Phase 2** | 재무 데이터 조회/저장 | 1일 |
| **Phase 3** | 위저드 단계 정의 조회 | 1일 |
| **Phase 4** | PMF 전체 (질문/제출/리포트) | 2일 |

---

## 문의

추가 질문이나 스펙 변경이 필요하면 연락 부탁드립니다.

- Swagger 문서: http://localhost:8080/swagger-ui.html
- 기존 API 문서: [API-REFERENCE.md](./API-REFERENCE.md)

