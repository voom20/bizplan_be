# [BE-AI-003] PMF AI 진단 API 연동

## 📌 우선순위
🟠 **Medium**

## 📋 요약
AI Engine의 `/api/v1/pmf/diagnose` API를 연동하여 AI 기반 PMF 진단 기능 구현

## 🎯 요구사항

### Backend 엔드포인트
```
POST /projects/{projectId}/pmf/ai-diagnose
🔒 Authorization Required
```

### AI Engine 호출
```
POST http://ai-engine:8000/api/v1/pmf/diagnose
```

### Request Body
```json
{
  "includeFinancialData": true
}
```

### Response (200 OK)
```json
{
  "projectId": "...",
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
      "description": "...",
      "level": "medium",
      "recommendation": "...",
      "relatedSections": ["market_analysis"]
    }
  ],
  "strengths": ["강점 1", "강점 2"],
  "summary": "종합 분석 요약...",
  "priorityActions": ["우선 조치 1", "우선 조치 2"],
  "modelUsed": "gemini-1.5-pro",
  "analysisTimeMs": 3500
}
```

### PMF 점수 등급
| Grade | 점수 범위 | 설명 |
|-------|----------|------|
| `excellent` | 80-100 | 뛰어남 |
| `good` | 60-79 | 좋음 |
| `fair` | 40-59 | 보통 |
| `poor` | 20-39 | 미흡 |
| `critical` | 0-19 | 심각 |

## ✅ 구현 체크리스트
- [ ] `PmfController`에 AI 진단 엔드포인트 추가
- [ ] `AiPmfDiagnosisRequest/Response` DTO 생성
- [ ] `AiEngineClient.diagnosePmf()` 메서드 구현
- [ ] `PmfService` AI 진단 로직 추가
- [ ] AI 결과 → PMF 리포트 변환/저장
- [ ] 에러 처리 (타임아웃, AI 장애)
- [ ] Swagger 문서화
- [ ] 통합 테스트

## 📎 관련 문서
- [BE-AI-ENGINE-INTEGRATION.md](./BE-AI-ENGINE-INTEGRATION.md)
- AI Engine ReDoc: http://localhost:8000/redoc

