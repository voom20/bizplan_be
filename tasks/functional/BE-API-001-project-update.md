# [BE-API-001] 프로젝트 수정 API 구현

## 📌 우선순위
🔴 **High**

## 📋 요약
프로젝트 제목 및 메타데이터를 수정할 수 있는 API 엔드포인트 구현

## 🎯 요구사항

### 엔드포인트
```
PATCH /projects/{projectId}
🔒 Authorization Required
```

### Request Body
```json
{
  "title": "수정된 프로젝트명",
  "status": "IN_PROGRESS"
}
```

### Response (200 OK)
```json
{
  "projectId": "project-uuid",
  "templateCode": "pre-startup",
  "title": "수정된 프로젝트명",
  "status": "IN_PROGRESS",
  "updatedAt": "2025-01-01T12:00:00"
}
```

### 에러 처리
| Status | Description |
|--------|-------------|
| 400 | 유효하지 않은 요청 데이터 |
| 401 | 인증 필요 |
| 403 | 권한 없음 (본인 프로젝트가 아님) |
| 404 | 프로젝트 없음 |

## ✅ 구현 체크리스트
- [ ] ProjectController에 PATCH 엔드포인트 추가
- [ ] ProjectUpdateRequest DTO 생성
- [ ] ProjectService에 updateProject 메서드 구현
- [ ] 소유권 검증 로직 추가
- [ ] 단위 테스트 작성
- [ ] Swagger 문서화

## 🔗 사용처
- 프로젝트 설정 페이지
- 프로젝트 이름 변경 기능

## 📎 관련 문서
- [BE-API-REQUEST.md](./BE-API-REQUEST.md)

