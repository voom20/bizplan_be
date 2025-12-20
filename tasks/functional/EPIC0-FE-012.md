# EPIC0-FE-012: 재무 추정 미리보기 (독립형) UI PoC

## 1. 개요
- **목표**: 프로젝트 생성 없이 재무 추정 결과를 미리 확인할 수 있는 독립형 계산기 페이지를 구현한다. 사용자가 프로젝트 생성 전에 재무 시뮬레이션을 경험할 수 있도록 한다.
- **범위**:
  - 랜딩 페이지 또는 별도 메뉴에서 접근 가능한 재무 계산기
  - 프로젝트 연동 없는 독립 실행 모드
  - 계산 결과 시각화 (BEP, LTV/CAC 차트)
  - "프로젝트로 저장하기" CTA 버튼 (로그인 유도)
- **Out of Scope**: 계산 결과 저장(비로그인), 고급 재무 모델링.

## 2. 상세 요구사항
- **접근성**: 비로그인 사용자도 접근 가능.
- **입력 폼**: 초기 자본, 월 고정비, 객단가(ARPU), 고객 획득 비용(CAC), 이탈률 등 핵심 변수 입력.
- **실시간 계산**: 입력 값 변경 시 즉시 결과 업데이트 (Debounce 적용).
- **결과 시각화**: 손익분기점 월, 예상 수익 그래프, LTV/CAC 비율 게이지.
- **CTA**: "이 설정으로 프로젝트 시작하기" 버튼 → 로그인/회원가입 유도 → 프로젝트 생성 시 값 자동 채우기.

## 3. 기술 스택 및 도구
- React + TypeScript
- Recharts (차트)
- Zustand (임시 상태 저장)
- API: /financials/preview

---

```yaml
task_id: "EPIC0-FE-012"
title: "재무 추정 미리보기 (독립형) UI PoC 구현"
summary: >
  비로그인 사용자도 접근 가능한 독립형 재무 계산기를 구현하고,
  계산 결과를 프로젝트로 연결하는 전환 흐름을 제공한다.
type: "functional"

epic: "EPIC_0_FE_PROTOTYPE"
req_ids: ["REQ-FUNC-009", "REQ-FUNC-015"]
agent_profile: ["frontend"]

parallelizable: true
estimated_effort: "S"
priority: "Could"

inputs:
  description: "재무 가정 변수 (프로젝트 미연동)"
  fields:
    - name: "initialCapital"
      type: "number"
    - name: "averageRevenuePerUser"
      type: "number"
    - name: "customerAcquisitionCost"
      type: "number"

outputs:
  description: "재무 미리보기 결과"
  success:
    ui_state: "Preview Charts without Save"

steps_hint:
  - "FinancialCalculatorPage 컴포넌트 구현 (Public Route)"
  - "PreviewFinancialForm 컴포넌트 구현"
  - "FinancialPreviewController API 연동 (POST /financials/preview)"
  - "결과 차트 재사용 (EPIC0-FE-004 컴포넌트)"
  - "CTABanner 컴포넌트 구현 (로그인 유도)"
  - "URL 파라미터로 초기값 전달 기능 (선택)"

preconditions:
  - "EPIC0-FE-004의 재무 시각화 컴포넌트가 구현되어 있어야 한다."
  - "백엔드 FinancialPreviewController API가 구현되어 있어야 한다."

postconditions:
  - "비로그인 상태에서도 재무 계산 결과를 확인할 수 있다."
  - "'프로젝트로 저장하기' 클릭 시 로그인 페이지로 이동하고, 로그인 후 값이 유지된다."

dependencies: ["EPIC0-FE-004"]
```
