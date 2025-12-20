# [BE-API-004] 재무 데이터 조회 API 구현

## 📌 우선순위
🟠 **Medium**

## 📋 요약
저장된 재무 시뮬레이션 데이터를 조회하는 API 엔드포인트 구현

## 🎯 배경
현재 `/financials/generate`만 있고, 이전에 생성한 재무 데이터를 조회하는 API가 없음.
재접속 시 마지막 시뮬레이션 결과를 표시해야 함.

## 🎯 요구사항

### 엔드포인트
```
GET /projects/{projectId}/financials
🔒 Authorization Required
```

### Response (200 OK)
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

### Response (404 Not Found)
재무 데이터가 없는 경우
```json
{
  "message": "재무 데이터가 없습니다."
}
```

## ✅ 구현 체크리스트
- [ ] FinancialsController에 GET 엔드포인트 추가
- [ ] FinancialData 엔티티 설계 (또는 기존 구조 활용)
- [ ] FinancialsService에 getFinancials 메서드 구현
- [ ] Response DTO 생성
- [ ] 단위 테스트 작성
- [ ] Swagger 문서화

## 🔗 사용처
- FinancialSimulation 컴포넌트 (초기 로드)
- 사업계획서 재무 섹션

## 📎 관련 문서
- [BE-API-REQUEST.md](./BE-API-REQUEST.md)

