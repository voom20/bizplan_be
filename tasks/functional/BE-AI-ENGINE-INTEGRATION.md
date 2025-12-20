# Backend-AI Engine 연동 계획서

> 📅 작성일: 2025-12-20  
> 📌 목적: Backend Core(Java/Spring Boot)와 AI Engine(Python/FastAPI) 간 API 연동

---

## 1. AI Engine API 현황

### 기본 정보
- **URL**: `http://localhost:8000` (개발) / `${AI_ENGINE_URL}` (운영)
- **문서**: http://localhost:8000/redoc, http://localhost:8000/docs
- **버전**: 0.1.0

### API 목록

| # | 엔드포인트 | 메서드 | 설명 | 상태 |
|---|-----------|--------|------|------|
| 1 | `/health` | GET | 헬스 체크 | ✅ 연동됨 |
| 2 | `/ready` | GET | 준비 상태 체크 (LLM 연결) | ✅ 연동됨 |
| 3 | `/api/v1/bizplan/generate` | POST | 사업계획서 섹션 생성 | 🔲 미연동 |
| 4 | `/api/v1/bizplan/sections` | GET | 지원 섹션 목록 조회 | 🔲 미연동 |
| 5 | `/api/v1/pmf/diagnose` | POST | PMF 진단 | 🔲 미연동 |
| 6 | `/api/v1/pmf/criteria` | GET | PMF 평가 기준 조회 | 🔲 미연동 |

---

## 2. 연동 필요 API 상세

### 2.1 사업계획서 섹션 생성 API

#### AI Engine 스펙
```
POST /api/v1/bizplan/generate
```

#### Request Body
```json
{
  "project_id": "string",
  "template_code": "string",
  "section_type": "executive_summary | problem_definition | solution | market_analysis | business_model | competitive_analysis | marketing_strategy | team | financial_plan | milestones",
  "mode": "easy | expert",
  "context": { },
  "previous_sections": { "section_type": "content" },
  "additional_instructions": "string"
}
```

#### Response
```json
{
  "project_id": "string",
  "section": {
    "section_type": "string",
    "title": "string",
    "content": "string",
    "word_count": 0,
    "model_used": "string",
    "generation_time_ms": 0,
    "token_usage": { "input": 0, "output": 0 }
  },
  "suggestions": ["개선 제안 1", "개선 제안 2"],
  "consistency_warnings": ["일관성 경고 1"]
}
```

#### Backend 연동 계획
| 항목 | 내용 |
|------|------|
| BE 엔드포인트 | `POST /projects/{projectId}/bizplan/sections/{sectionType}` |
| Controller | `BizPlanController` (신규) |
| Service | `BizPlanOrchestrationService` 활용/확장 |
| Client | `AiEngineClient` 확장 |

---

### 2.2 지원 섹션 목록 API

#### AI Engine 스펙
```
GET /api/v1/bizplan/sections
```

#### Response
```json
{
  "sections": [
    "executive_summary",
    "problem_definition",
    "solution",
    "market_analysis",
    "business_model",
    "competitive_analysis",
    "marketing_strategy",
    "team",
    "financial_plan",
    "milestones"
  ]
}
```

#### Backend 연동 계획
| 항목 | 내용 |
|------|------|
| BE 엔드포인트 | `GET /bizplan/sections` |
| Controller | `BizPlanController` (신규) |
| 캐싱 | 가능 (정적 데이터) |

---

### 2.3 PMF AI 진단 API

#### AI Engine 스펙
```
POST /api/v1/pmf/diagnose
```

#### Request Body
```json
{
  "project_id": "string",
  "context": { },
  "bizplan_sections": { "section_type": "content" },
  "financial_data": { }
}
```

#### Response
```json
{
  "project_id": "string",
  "overall_score": 75,
  "score_grade": "excellent | good | fair | poor | critical",
  "category_scores": {
    "market_fit": 80,
    "problem_solution": 70,
    "revenue_model": 75
  },
  "risks": [
    {
      "category": "market",
      "title": "시장 규모 불확실",
      "description": "...",
      "level": "low | medium | high | critical",
      "recommendation": "...",
      "related_sections": ["market_analysis"]
    }
  ],
  "strengths": ["강점 1", "강점 2"],
  "summary": "종합 분석 요약",
  "priority_actions": ["우선 조치 1", "우선 조치 2", "우선 조치 3"],
  "model_used": "gemini-1.5-pro",
  "analysis_time_ms": 3500
}
```

#### Backend 연동 계획
| 항목 | 내용 |
|------|------|
| BE 엔드포인트 | `POST /projects/{projectId}/pmf/ai-diagnose` |
| Controller | `PmfController` 확장 |
| Service | `PmfService` 확장 |
| Client | `AiEngineClient` 확장 |
| 저장 | PMF 리포트로 저장 |

---

### 2.4 PMF 평가 기준 조회 API

#### AI Engine 스펙
```
GET /api/v1/pmf/criteria
```

#### Response
```json
{
  "criteria": [
    {
      "category": "market_fit",
      "name": "시장 적합성",
      "weight": 0.3,
      "description": "..."
    }
  ]
}
```

#### Backend 연동 계획
| 항목 | 내용 |
|------|------|
| BE 엔드포인트 | `GET /pmf/criteria` |
| Controller | `PmfController` 확장 |
| 캐싱 | 가능 (정적 데이터) |

---

## 3. 구현 작업 목록

### Phase 1: 사업계획서 생성 (🔴 High Priority)

| # | 작업 | 예상 공수 | 담당 |
|---|------|----------|------|
| 1 | `BizPlanController` 생성 | 0.5일 | BE |
| 2 | `AiEngineClient.generateSection()` 구현 | 0.5일 | BE |
| 3 | `BizPlanSectionRequest/Response` DTO 생성 | 0.25일 | BE |
| 4 | 생성된 섹션 저장 로직 | 0.5일 | BE |
| 5 | 섹션 조회/수정 API | 0.5일 | BE |
| 6 | 통합 테스트 | 0.5일 | BE |

### Phase 2: PMF AI 진단 (🟠 Medium Priority)

| # | 작업 | 예상 공수 | 담당 |
|---|------|----------|------|
| 1 | `PmfController` AI 진단 엔드포인트 추가 | 0.25일 | BE |
| 2 | `AiEngineClient.diagnosePmf()` 구현 | 0.5일 | BE |
| 3 | AI 진단 결과 → PMF 리포트 변환 | 0.5일 | BE |
| 4 | 통합 테스트 | 0.5일 | BE |

### Phase 3: 보조 API (🟡 Low Priority)

| # | 작업 | 예상 공수 | 담당 |
|---|------|----------|------|
| 1 | 섹션 목록 조회 API | 0.25일 | BE |
| 2 | PMF 평가 기준 조회 API | 0.25일 | BE |

---

## 4. 기술 구현 상세

### 4.1 AiEngineClient 확장

```java
// 현재 구현된 AiEngineClient 위치
// src/main/java/com/vibe/bizplan/bizplan_be/infrastructure/client/AiEngineClient.java

// 추가할 메서드
public interface AiEngineClient {
    
    // 사업계획서 섹션 생성
    BizPlanSectionResponse generateSection(BizPlanSectionRequest request);
    
    // 지원 섹션 목록
    List<String> getSupportedSections();
    
    // PMF AI 진단
    AiPmfDiagnosisResponse diagnosePmf(AiPmfDiagnosisRequest request);
    
    // PMF 평가 기준
    List<PmfCriteriaResponse> getPmfCriteria();
}
```

### 4.2 DTO 매핑

```
AI Engine                    Backend
─────────────────────────    ─────────────────────────
BizPlanGenerateRequest   →   BizPlanSectionRequest (신규)
BizPlanGenerateResponse  ←   BizPlanSectionResponse (신규)
PMFDiagnosticRequest     →   AiPmfDiagnosisRequest (신규)
PMFDiagnosticResponse    ←   AiPmfDiagnosisResponse (신규)
```

### 4.3 에러 처리

| AI Engine 상태 | Backend 처리 |
|---------------|-------------|
| 200 OK | 정상 응답 |
| 422 Validation Error | `400 Bad Request` 변환 |
| 500 Internal Error | `503 Service Unavailable` + 재시도 |
| Timeout | `504 Gateway Timeout` + 폴백 |

### 4.4 타임아웃 설정

```properties
# application.properties
ai.engine.url=${AI_ENGINE_URL:http://localhost:8000}
ai.engine.timeout=60  # 초 (LLM 생성 시간 고려)
ai.engine.connect-timeout=5
ai.engine.read-timeout=60
```

---

## 5. 데이터 모델 추가

### 5.1 BizPlanSection 엔티티 (신규)

```sql
-- V6__create_bizplan_section_table.sql
CREATE TABLE bizplan_sections (
    id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    section_type VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    content TEXT,
    word_count INT,
    model_used VARCHAR(50),
    generation_time_ms INT,
    version INT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_bizplan_section_project 
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    UNIQUE KEY uk_project_section (project_id, section_type)
);
```

---

## 6. API 설계 (Backend)

### 6.1 사업계획서 API

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/projects/{projectId}/bizplan/sections/{sectionType}/generate` | 섹션 생성 (AI) |
| GET | `/projects/{projectId}/bizplan/sections` | 전체 섹션 조회 |
| GET | `/projects/{projectId}/bizplan/sections/{sectionType}` | 특정 섹션 조회 |
| PUT | `/projects/{projectId}/bizplan/sections/{sectionType}` | 섹션 수정 |
| DELETE | `/projects/{projectId}/bizplan/sections/{sectionType}` | 섹션 삭제 |
| GET | `/bizplan/section-types` | 지원 섹션 타입 목록 |

### 6.2 PMF AI 진단 API

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/projects/{projectId}/pmf/ai-diagnose` | AI 기반 PMF 진단 |
| GET | `/pmf/criteria` | PMF 평가 기준 조회 |

---

## 7. 우선순위 및 일정

| 단계 | 내용 | 예상 기간 | 우선순위 |
|------|------|----------|----------|
| **Phase 1** | 사업계획서 섹션 생성 API | 2.5일 | 🔴 High |
| **Phase 2** | PMF AI 진단 연동 | 1.5일 | 🟠 Medium |
| **Phase 3** | 보조 API (목록/기준 조회) | 0.5일 | 🟡 Low |
| **총계** | | **4.5일** | |

---

## 8. 테스트 계획

### 8.1 단위 테스트
- `AiEngineClientTest`: Mock 서버로 API 호출 테스트
- `BizPlanServiceTest`: 섹션 생성/저장 로직 테스트

### 8.2 통합 테스트
- AI Engine 실제 연동 테스트 (테스트 환경)
- 타임아웃 및 에러 처리 테스트

### 8.3 성능 테스트
- 섹션 생성 응답 시간 (목표: p95 < 10초)
- 동시 요청 처리 테스트

---

## 9. 리스크 및 대응

| 리스크 | 영향 | 대응 방안 |
|--------|------|----------|
| AI Engine 응답 지연 | 사용자 경험 저하 | 비동기 처리 + 폴링 |
| LLM 토큰 비용 | 운영 비용 증가 | 캐싱 + 요청 최적화 |
| AI Engine 장애 | 핵심 기능 불가 | 폴백 메시지 + 알림 |

---

## 10. 참고 문서

- AI Engine API 문서: http://localhost:8000/redoc
- Backend API 문서: http://localhost:8080/swagger-ui.html
- [기존 AiEngineClient 코드](../src/main/java/com/vibe/bizplan/bizplan_be/infrastructure/client/AiEngineClient.java)

---

## 문의

추가 질문이나 스펙 변경이 필요하면 연락 부탁드립니다.

