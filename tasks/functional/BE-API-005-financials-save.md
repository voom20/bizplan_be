# [BE-API-005] 재무 데이터 저장 API 구현

## 📌 우선순위
🟠 **Medium**

## 📋 요약
재무 가정값만 저장 (시뮬레이션 재실행 없이)하는 API 엔드포인트 구현

## 🎯 배경
현재 `/financials/generate`는 항상 시뮬레이션을 다시 실행함.
가정값만 저장하고 싶을 때 사용.

## 🎯 요구사항

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

### Response (200 OK)
```json
{
  "message": "저장되었습니다.",
  "updatedAt": "2025-01-01T12:00:00"
}
```

### 에러 처리
| Status | Description |
|--------|-------------|
| 400 | 유효하지 않은 가정값 |
| 401 | 인증 필요 |
| 403 | 권한 없음 |
| 404 | 프로젝트 없음 |

## ✅ 구현 체크리스트
- [ ] FinancialsController에 PUT 엔드포인트 추가
- [ ] FinancialAssumptionsRequest DTO 생성
- [ ] FinancialsService에 saveAssumptions 메서드 구현
- [ ] 유효성 검증 로직 추가
- [ ] 단위 테스트 작성
- [ ] Swagger 문서화

## 🔗 사용처
- 자동 저장 기능 (Auto-save)
- 임시 저장 버튼

## 📎 관련 문서
- [BE-API-REQUEST.md](./BE-API-REQUEST.md)

