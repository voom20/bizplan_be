# [BE-AI-004] AI Engine 보조 API 연동

## 📌 우선순위
🟡 **Low**

## 📋 요약
AI Engine의 보조 API (섹션 목록, PMF 평가 기준) 연동

## 🎯 요구사항

### 1. 섹션 타입 목록 조회

```
GET /bizplan/section-types
```

#### AI Engine 호출
```
GET http://ai-engine:8000/api/v1/bizplan/sections
```

#### Response
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
    }
  ]
}
```

### 2. PMF 평가 기준 조회

```
GET /pmf/criteria
```

#### AI Engine 호출
```
GET http://ai-engine:8000/api/v1/pmf/criteria
```

#### Response
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

## ✅ 구현 체크리스트
- [ ] `BizPlanController`에 섹션 타입 목록 엔드포인트 추가
- [ ] `PmfController`에 평가 기준 엔드포인트 추가
- [ ] `AiEngineClient` 메서드 추가
- [ ] 응답 캐싱 (정적 데이터)
- [ ] Swagger 문서화

## 📎 관련 문서
- [BE-AI-ENGINE-INTEGRATION.md](./BE-AI-ENGINE-INTEGRATION.md)

