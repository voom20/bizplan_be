# [BE-API-008] PMF 리포트 조회 API 구현

## 📌 우선순위
🟡 **Low**

## 📋 요약
저장된 PMF 분석 결과를 조회하는 API 엔드포인트 구현

## 🎯 요구사항

### 엔드포인트
```
GET /projects/{projectId}/pmf/report
🔒 Authorization Required
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
  "answers": [
    { "questionId": "pmf-1", "value": 1 },
    { "questionId": "pmf-2", "value": [1, 2] }
  ],
  "createdAt": "2025-01-01T12:00:00"
}
```

### Response (404 Not Found)
PMF 리포트가 없는 경우
```json
{
  "message": "PMF 리포트가 없습니다."
}
```

## ✅ 구현 체크리스트
- [ ] PmfController에 GET 엔드포인트 추가
- [ ] PmfReportResponse DTO 생성
- [ ] PmfService에 getReport 메서드 구현
- [ ] 단위 테스트 작성
- [ ] Swagger 문서화

## 🔗 사용처
- PMFSurvey 컴포넌트 (이전 결과 로드)
- 사업계획서 PMF 섹션

## 📎 관련 문서
- [BE-API-REQUEST.md](./BE-API-REQUEST.md)

