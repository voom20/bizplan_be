# EPIC0-FE-011: 공통 컴포넌트 및 전역 에러 핸들링 UI PoC

## 1. 개요
- **목표**: 애플리케이션 전반에서 사용되는 공통 UI 컴포넌트와 전역 에러 핸들링 시스템을 구현한다.
- **범위**:
  - Toast 알림 시스템 (성공, 에러, 경고, 정보)
  - 전역 에러 바운더리 (React Error Boundary)
  - 404 Not Found 페이지
  - 500 Server Error 페이지
  - API 에러 핸들링 유틸리티
  - 로딩 상태 컴포넌트 (Spinner, Skeleton)
  - 공통 모달 컴포넌트
- **Out of Scope**: 디자인 시스템 전체 구축, 테마 시스템.

## 2. 상세 요구사항
- **Toast 시스템**: 화면 우상단에 스택형 토스트 표시, 자동 사라짐(3초), 수동 닫기 지원.
- **에러 바운더리**: 예기치 않은 렌더링 오류 시 폴백 UI 표시, 에러 리포트 버튼 제공.
- **404 페이지**: 친근한 일러스트와 함께 홈으로 돌아가기 CTA.
- **500 페이지**: "문제가 발생했습니다" 메시지, 다시 시도/홈으로 버튼.
- **API 에러**: 상태 코드별 메시지 매핑 (400: 입력 오류, 401: 재로그인 필요, 403: 권한 없음, 404: 찾을 수 없음, 500: 서버 오류).
- **로딩 컴포넌트**: Spinner(버튼용), Skeleton(카드/리스트용), FullPageLoader(페이지 전환용).

## 3. 기술 스택 및 도구
- React Error Boundary
- React-Hot-Toast 또는 Sonner (토스트)
- Zustand 또는 Context (전역 상태)
- Tailwind CSS (스타일링)

---

```yaml
task_id: "EPIC0-FE-011"
title: "공통 컴포넌트 및 전역 에러 핸들링 UI PoC 구현"
summary: >
  Toast 알림, 에러 페이지, API 에러 핸들링 등
  애플리케이션 전반에서 사용하는 공통 UI 인프라를 구축한다.
type: "functional"

epic: "EPIC_0_FE_PROTOTYPE"
req_ids: ["REQ-NF-006-SEC", "REQ-NF-012-OPS"]
agent_profile: ["frontend"]

parallelizable: true
estimated_effort: "M"
priority: "Must"

inputs:
  description: "에러 및 알림 데이터"
  fields:
    - name: "error"
      type: "object"
      example: { code: 400, message: "잘못된 입력입니다" }
    - name: "toast"
      type: "object"
      example: { type: "success", message: "저장되었습니다" }

outputs:
  description: "공통 UI 컴포넌트 라이브러리"
  success:
    components: ["Toast", "ErrorBoundary", "NotFoundPage", "ServerErrorPage", "LoadingSpinner", "Modal"]

steps_hint:
  - "ToastProvider 및 useToast 훅 구현"
  - "GlobalErrorBoundary 컴포넌트 구현"
  - "NotFoundPage (404) 컴포넌트 구현"
  - "ServerErrorPage (500) 컴포넌트 구현"
  - "apiErrorHandler 유틸 함수 구현 (Axios Interceptor 연동)"
  - "LoadingSpinner, SkeletonCard, FullPageLoader 컴포넌트 구현"
  - "ConfirmModal, AlertModal 공통 모달 구현"

preconditions:
  - "React+Vite 기본 프로젝트가 셋업되어 있어야 한다."

postconditions:
  - "API 오류 발생 시 적절한 토스트 메시지가 표시된다."
  - "렌더링 오류 발생 시 폴백 UI가 표시된다."
  - "존재하지 않는 URL 접근 시 404 페이지가 표시된다."

dependencies: []
```
