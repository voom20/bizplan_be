# EPIC0-FE-007: 사용자 프로필 관리 UI PoC

## 1. 개요
- **목표**: 로그인한 사용자가 자신의 프로필을 조회/수정하고, 비밀번호 변경 및 회원 탈퇴를 할 수 있는 화면을 구현한다.
- **범위**:
  - 프로필 조회 페이지 (내 정보 확인)
  - 프로필 수정 폼 (이름, 연락처 등)
  - 비밀번호 변경 폼
  - 회원 탈퇴 확인 모달
- **Out of Scope**: 프로필 이미지 업로드, 이메일 변경.

## 2. 상세 요구사항
- **프로필 조회**: 현재 사용자의 이메일, 이름, 가입일 등 표시.
- **프로필 수정**: 이름, 연락처 등 편집 가능 필드 제공, 저장 시 실시간 피드백.
- **비밀번호 변경**: 현재 비밀번호 확인 → 새 비밀번호 입력 → 확인, 강도 표시기(Strength Meter) 포함.
- **회원 탈퇴**: 비밀번호 재확인 후 탈퇴 처리, 탈퇴 전 경고 모달 표시.
- **접근 제어**: 인증된 사용자만 접근 가능 (ProtectedRoute).

## 3. 기술 스택 및 도구
- React Hook Form + Zod (폼 검증)
- UI Components (Modal, Toast 알림)
- useAuthStore (현재 사용자 정보)

---

```yaml
task_id: "EPIC0-FE-007"
title: "사용자 프로필 관리 UI PoC 구현"
summary: >
  로그인한 사용자의 프로필 조회/수정, 비밀번호 변경,
  회원 탈퇴 기능을 제공하는 설정 페이지를 구현한다.
type: "functional"

epic: "EPIC_0_FE_PROTOTYPE"
req_ids: ["REQ-FUNC-USER", "REQ-NF-SEC-001"]
agent_profile: ["frontend"]

parallelizable: true
estimated_effort: "S"
priority: "Should"

inputs:
  description: "사용자 프로필 및 비밀번호 정보"
  fields:
    - name: "displayName"
      type: "string"
    - name: "currentPassword"
      type: "string"
    - name: "newPassword"
      type: "string"

outputs:
  description: "업데이트된 프로필 정보"
  success:
    ui_state: "Profile Updated, Toast Notification"

steps_hint:
  - "ProfilePage 컴포넌트 구현 (탭 형태: 프로필, 보안)"
  - "ProfileEditForm 컴포넌트 구현"
  - "ChangePasswordForm 컴포넌트 구현 (Strength Meter 포함)"
  - "DeleteAccountModal 컴포넌트 구현"
  - "UserController API 연동 (/api/v1/users/me)"

preconditions:
  - "EPIC0-FE-006의 인증 흐름이 구현되어 있어야 한다."
  - "백엔드 UserController API가 구현되어 있어야 한다."

postconditions:
  - "프로필 수정 성공 시 성공 토스트 메시지가 표시된다."
  - "비밀번호 변경 성공 시 재로그인을 요청한다."
  - "회원 탈퇴 완료 시 로그아웃 후 홈으로 이동한다."

dependencies: ["EPIC0-FE-006"]
```
