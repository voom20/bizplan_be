# [BE-AI-001] 사업계획서 섹션 생성 API 연동

## 📌 우선순위
🔴 **High**

## 📋 요약
AI Engine의 `/api/v1/bizplan/generate` API를 연동하여 사업계획서 섹션을 AI로 생성하는 기능 구현

## 🎯 요구사항

### Backend 엔드포인트
```
POST /projects/{projectId}/bizplan/sections/{sectionType}/generate
🔒 Authorization Required
```

### AI Engine 호출
```
POST http://ai-engine:8000/api/v1/bizplan/generate
```

### Request Body
```json
{
  "mode": "easy",
  "additionalInstructions": "선택적 추가 지시사항"
}
```

### Response (200 OK)
```json
{
  "projectId": "...",
  "sectionType": "executive_summary",
  "title": "사업개요",
  "content": "AI 생성 콘텐츠...",
  "wordCount": 500,
  "modelUsed": "gemini-1.5-pro",
  "generationTimeMs": 3500,
  "suggestions": ["개선 제안 1"],
  "consistencyWarnings": []
}
```

### 지원 섹션 타입
- `executive_summary` - 사업개요
- `problem_definition` - 문제 정의
- `solution` - 솔루션
- `market_analysis` - 시장 분석
- `business_model` - 비즈니스 모델
- `competitive_analysis` - 경쟁 분석
- `marketing_strategy` - 마케팅 전략
- `team` - 팀 소개
- `financial_plan` - 재무 계획
- `milestones` - 마일스톤

## ✅ 구현 체크리스트
- [ ] `BizPlanController` 생성
- [ ] `BizPlanSectionRequest/Response` DTO 생성
- [ ] `AiEngineClient.generateSection()` 메서드 구현
- [ ] `BizPlanService` 생성 (섹션 저장/조회)
- [ ] `BizPlanSection` 엔티티 생성
- [ ] V6 마이그레이션: bizplan_sections 테이블
- [ ] 에러 처리 (타임아웃, AI 장애)
- [ ] Swagger 문서화
- [ ] 통합 테스트

## 📎 관련 문서
- [BE-AI-ENGINE-INTEGRATION.md](./BE-AI-ENGINE-INTEGRATION.md)
- AI Engine ReDoc: http://localhost:8000/redoc

