# EPIC0-FE-009: 문서 버전 관리 및 히스토리 UI PoC

## 1. 개요
- **목표**: 생성된 사업계획서의 버전 목록을 조회하고, 특정 버전을 선택하여 비교하거나 복원할 수 있는 화면을 구현한다.
- **범위**:
  - 문서 버전 목록 패널 (사이드바 또는 드롭다운)
  - 버전별 생성 일시, 변경 내용 요약 표시
  - 특정 버전 선택 시 해당 버전 내용 미리보기
  - 버전 비교 뷰 (Diff View) - 간단한 텍스트 비교
- **Out of Scope**: 버전 병합(Merge), 협업 편집 기능.

## 2. 상세 요구사항
- **버전 목록**: 버전 번호, 생성 일시, 상태(DRAFT/COMPLETED) 표시.
- **현재 버전 표시**: 최신 버전에 "현재" 뱃지 표시.
- **버전 선택**: 클릭 시 해당 버전의 문서 내용을 뷰어에 로드.
- **섹션 재생성**: 특정 섹션의 "AI 다시 쓰기" 버튼 클릭 시 해당 섹션만 재생성 (새 버전 생성).
- **버전 비교**: 두 버전 선택 시 변경된 부분 하이라이트 (선택적 기능).

## 3. 기술 스택 및 도구
- React + TypeScript
- Diff 라이브러리 (diff, react-diff-viewer 등)
- UI Components (Timeline, Badge)

---

```yaml
task_id: "EPIC0-FE-009"
title: "문서 버전 관리 및 히스토리 UI PoC 구현"
summary: >
  사업계획서의 버전 목록을 표시하고, 버전 선택/비교
  기능을 제공하는 버전 관리 UI를 구현한다.
type: "functional"

epic: "EPIC_0_FE_PROTOTYPE"
req_ids: ["REQ-FUNC-003", "REQ-FUNC-005"]
agent_profile: ["frontend"]

parallelizable: true
estimated_effort: "S"
priority: "Should"

inputs:
  description: "문서 버전 목록"
  fields:
    - name: "versions"
      type: "array"
      example: [{version: 1, createdAt: "2025-01-01T10:00:00Z", status: "COMPLETED"}]

outputs:
  description: "버전 히스토리 UI"
  success:
    ui_state: "Version List with Preview"

steps_hint:
  - "VersionHistoryPanel 컴포넌트 구현 (사이드바 형태)"
  - "VersionListItem 컴포넌트 구현"
  - "버전 선택 시 DocumentViewer에 해당 내용 로드"
  - "SectionRegenerateButton 컴포넌트 구현"
  - "BusinessPlanController API 연동 (/projects/{id}/documents/business-plan/versions)"

preconditions:
  - "EPIC0-FE-003의 문서 뷰어가 구현되어 있어야 한다."
  - "백엔드 BusinessPlanController API가 구현되어 있어야 한다."

postconditions:
  - "버전 목록에서 과거 버전을 선택하면 해당 내용이 표시된다."
  - "섹션 재생성 시 새 버전이 생성되고 목록에 추가된다."

dependencies: ["EPIC0-FE-003"]
```
