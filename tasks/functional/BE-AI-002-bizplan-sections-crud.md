# [BE-AI-002] 사업계획서 섹션 CRUD API 구현

## 📌 우선순위
🔴 **High**

## 📋 요약
생성된 사업계획서 섹션의 조회, 수정, 삭제 기능 구현

## 🎯 요구사항

### 엔드포인트

| 메서드 | URL | 설명 |
|--------|-----|------|
| GET | `/projects/{projectId}/bizplan/sections` | 전체 섹션 조회 |
| GET | `/projects/{projectId}/bizplan/sections/{sectionType}` | 특정 섹션 조회 |
| PUT | `/projects/{projectId}/bizplan/sections/{sectionType}` | 섹션 수정 |
| DELETE | `/projects/{projectId}/bizplan/sections/{sectionType}` | 섹션 삭제 |

### Response - 전체 섹션 조회
```json
{
  "projectId": "...",
  "sections": [
    {
      "sectionType": "executive_summary",
      "title": "사업개요",
      "content": "...",
      "wordCount": 500,
      "updatedAt": "2025-01-01T12:00:00"
    }
  ],
  "totalSections": 3,
  "completedSections": 2
}
```

### Request - 섹션 수정
```json
{
  "title": "수정된 제목",
  "content": "수정된 내용..."
}
```

## ✅ 구현 체크리스트
- [ ] `BizPlanController`에 CRUD 엔드포인트 추가
- [ ] `BizPlanSectionRepository` 생성
- [ ] `BizPlanService` CRUD 메서드 구현
- [ ] 소유권 검증 로직
- [ ] 버전 관리 (수정 이력)
- [ ] Swagger 문서화
- [ ] 단위 테스트

## 📎 관련 문서
- [BE-AI-ENGINE-INTEGRATION.md](./BE-AI-ENGINE-INTEGRATION.md)

