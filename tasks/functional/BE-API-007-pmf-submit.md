# [BE-API-007] PMF 설문 결과 제출 API 구현

## 📌 우선순위
🟡 **Low**

## 📋 요약
PMF 설문 답변을 제출하고 분석 결과를 생성하는 API 엔드포인트 구현

## 🎯 요구사항

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

### Response (200 OK)
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

## ✅ 구현 체크리스트
- [ ] PmfController에 POST 엔드포인트 추가
- [ ] PmfSubmitRequest DTO 생성
- [ ] PmfReport 엔티티 설계
- [ ] PMF 점수 계산 로직 구현
- [ ] 리스크/추천사항 생성 로직 구현
- [ ] PmfService 구현
- [ ] 단위 테스트 작성
- [ ] Swagger 문서화

## 🔗 사용처
- PMFSurvey 컴포넌트 (결과 제출)

## 📎 관련 문서
- [BE-API-REQUEST.md](./BE-API-REQUEST.md)

