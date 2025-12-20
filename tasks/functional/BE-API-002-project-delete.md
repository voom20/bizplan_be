# [BE-API-002] 프로젝트 삭제 API 구현

## 📌 우선순위
🔴 **High**

## 📋 요약
사용자가 자신의 프로젝트를 삭제할 수 있는 API 엔드포인트 구현

## 🎯 요구사항

### 엔드포인트
```
DELETE /projects/{projectId}
🔒 Authorization Required
```

### Response (204 No Content)
성공 시 본문 없음

### 에러 처리
| Status | Description |
|--------|-------------|
| 401 | 인증 필요 |
| 403 | 권한 없음 (본인 프로젝트가 아님) |
| 404 | 프로젝트 없음 |

## ✅ 구현 체크리스트
- [ ] ProjectController에 DELETE 엔드포인트 추가
- [ ] ProjectService에 deleteProject 메서드 구현
- [ ] 소유권 검증 로직 추가
- [ ] 관련 데이터 삭제 처리 (Wizard Answers, Documents 등)
- [ ] Soft Delete vs Hard Delete 결정
- [ ] 단위 테스트 작성
- [ ] Swagger 문서화

## 🔗 사용처
- 프로젝트 목록 페이지 (삭제 버튼)
- 프로젝트 설정 페이지

## 📎 관련 문서
- [BE-API-REQUEST.md](./BE-API-REQUEST.md)

