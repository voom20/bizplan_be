# EPIC0-FE-010: 문서 내보내기 실제 구현 UI PoC

## 1. 개요
- **목표**: 생성된 사업계획서를 PDF 또는 HTML 형식으로 실제 다운로드할 수 있는 기능을 구현한다. (FE-003의 Mock에서 실제 연동으로 확장)
- **범위**:
  - 내보내기 형식 선택 드롭다운 (PDF, HTML)
  - 다운로드 진행 상태 표시 (로딩 인디케이터)
  - 다운로드 완료/실패 토스트 알림
  - 지원 형식 안내 (HWP 향후 지원 예정 메시지)
  - 특정 버전 내보내기 옵션
- **Out of Scope**: HWP 형식 실제 구현, 인쇄 기능.

## 2. 상세 요구사항
- **형식 선택**: PDF(기본), HTML 선택 가능, 각 형식 아이콘 표시.
- **다운로드 흐름**: 버튼 클릭 → 로딩 표시 → 파일 다운로드 트리거 → 완료 토스트.
- **에러 처리**: 문서 없음(404), 서버 오류(500) 등에 대한 에러 메시지 표시.
- **버전 선택**: 현재 버전 또는 특정 버전 선택 후 내보내기.
- **파일명**: "프로젝트명_사업계획서_v버전.pdf" 형태로 자동 생성.

## 3. 기술 스택 및 도구
- File Download 처리 (Blob, createObjectURL)
- Axios (바이너리 데이터 처리)
- UI Components (Dropdown, Toast, LoadingSpinner)

---

```yaml
task_id: "EPIC0-FE-010"
title: "문서 내보내기 실제 구현 UI PoC 구현"
summary: >
  사업계획서를 PDF/HTML 형식으로 실제 다운로드하는
  기능을 구현하고, 형식 선택 및 버전 옵션을 제공한다.
type: "functional"

epic: "EPIC_0_FE_PROTOTYPE"
req_ids: ["REQ-FUNC-011", "REQ-FUNC-006"]
agent_profile: ["frontend"]

parallelizable: true
estimated_effort: "S"
priority: "Must"

inputs:
  description: "내보내기 옵션"
  fields:
    - name: "format"
      type: "string"
      example: "pdf"
    - name: "version"
      type: "number"
      example: 1

outputs:
  description: "다운로드된 파일"
  success:
    file: "사업계획서_v1.pdf"

steps_hint:
  - "ExportButton 컴포넌트를 ExportDropdown으로 확장"
  - "FormatSelector 컴포넌트 구현"
  - "useExportDocument 훅 구현 (Blob 처리 포함)"
  - "downloadFile 유틸 함수 구현 (createObjectURL)"
  - "ExportController API 연동 (GET /projects/{id}/export)"
  - "에러 핸들링 및 토스트 알림 연동"

preconditions:
  - "EPIC0-FE-003의 문서 뷰어가 구현되어 있어야 한다."
  - "백엔드 ExportController API가 구현되어 있어야 한다."

postconditions:
  - "PDF 선택 시 PDF 파일이 다운로드된다."
  - "HTML 선택 시 HTML 파일이 다운로드된다."
  - "다운로드 실패 시 에러 토스트가 표시된다."

dependencies: ["EPIC0-FE-003"]
```
