# EPIC0-FE-008: 프로젝트 대시보드 및 목록 관리 UI PoC

## 1. 개요
- **목표**: 로그인한 사용자가 자신의 프로젝트 목록을 조회하고, 각 프로젝트의 상태를 한눈에 파악할 수 있는 대시보드를 구현한다.
- **범위**:
  - 프로젝트 목록 페이지 (카드 또는 테이블 레이아웃)
  - 프로젝트 상태 표시 (진행 중, 완료, 초안 등)
  - 프로젝트 검색 및 필터링
  - 프로젝트 정렬 (최근 수정일, 생성일)
  - 빈 상태(Empty State) UI
- **Out of Scope**: 프로젝트 삭제/복사, 팀 공유 기능.

## 2. 상세 요구사항
- **목록 표시**: 각 프로젝트 카드에 제목, 템플릿 유형, 상태, 마지막 수정일, 완료율(Progress) 표시.
- **빈 상태**: 프로젝트가 없을 경우 "새 프로젝트 만들기" CTA 버튼과 안내 문구 표시.
- **검색**: 프로젝트 제목으로 필터링 (클라이언트 사이드).
- **정렬**: 드롭다운으로 정렬 기준 선택 (최근 수정순, 생성순, 이름순).
- **상태 필터**: 전체, 진행 중, 완료 탭 제공.
- **프로젝트 진입**: 카드 클릭 시 해당 프로젝트 Wizard 또는 문서 뷰어로 이동.

## 3. 기술 스택 및 도구
- React + TypeScript
- TanStack Query (React Query) - 데이터 페칭 및 캐싱
- UI Components (Card, Badge, SearchInput, Dropdown)

---

```yaml
task_id: "EPIC0-FE-008"
title: "프로젝트 대시보드 및 목록 관리 UI PoC 구현"
summary: >
  사용자의 프로젝트 목록을 카드 형태로 표시하고,
  검색/필터/정렬 기능을 제공하는 대시보드를 구현한다.
type: "functional"

epic: "EPIC_0_FE_PROTOTYPE"
req_ids: ["REQ-FUNC-001", "REQ-FUNC-014"]
agent_profile: ["frontend"]

parallelizable: true
estimated_effort: "M"
priority: "Must"

inputs:
  description: "프로젝트 목록 API 응답"
  fields:
    - name: "projects"
      type: "array"
      example: [{projectId: "proj_123", title: "예비창업 1호", status: "IN_PROGRESS"}]

outputs:
  description: "프로젝트 대시보드 UI"
  success:
    ui_state: "Project List with Filters"

steps_hint:
  - "DashboardPage 컴포넌트 구현"
  - "ProjectCard 컴포넌트 구현 (상태 뱃지, 진행률 바 포함)"
  - "SearchBar 및 SortDropdown 컴포넌트 구현"
  - "EmptyState 컴포넌트 구현 (CTA 버튼 포함)"
  - "useProjects 훅 구현 (React Query 연동)"
  - "ProjectController API 연동 (GET /projects)"

preconditions:
  - "EPIC0-FE-006의 인증 흐름이 구현되어 있어야 한다."
  - "EPIC0-FE-001의 프로젝트 생성 기능이 구현되어 있어야 한다."

postconditions:
  - "로그인 후 대시보드에서 자신의 프로젝트 목록을 볼 수 있다."
  - "프로젝트 카드 클릭 시 해당 프로젝트 페이지로 이동한다."
  - "검색어 입력 시 실시간으로 목록이 필터링된다."

dependencies: ["EPIC0-FE-006", "EPIC0-FE-001"]
```
