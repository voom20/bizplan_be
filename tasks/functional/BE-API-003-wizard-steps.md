# [BE-API-003] 위저드 단계 정의 조회 API 구현

## 📌 우선순위
🟠 **Medium**

## 📋 요약
템플릿별 위저드 단계 및 질문 정의를 조회하는 API 엔드포인트 구현

## 🎯 배경
현재 프론트엔드에서 `mockData.ts`의 하드코딩된 `wizardSteps`를 사용 중.
템플릿마다 다른 질문 세트가 필요할 수 있음.

## 🎯 요구사항

### 엔드포인트 (Option 1: 프로젝트 기반)
```
GET /projects/{projectId}/wizard/steps
🔒 Authorization Required
```

### 엔드포인트 (Option 2: 템플릿 기반)
```
GET /templates/{templateCode}/wizard/steps
```

### Response (200 OK)
```json
{
  "templateCode": "pre-startup",
  "totalSteps": 5,
  "steps": [
    {
      "id": 1,
      "title": "사업 아이디어",
      "description": "사업 아이디어를 설명해주세요",
      "questions": [
        {
          "id": "business-name",
          "type": "text",
          "label": "사업명",
          "placeholder": "사업명을 입력하세요",
          "required": true,
          "maxLength": 100
        },
        {
          "id": "business-description",
          "type": "textarea",
          "label": "사업 설명",
          "placeholder": "사업 아이디어를 설명해주세요",
          "required": true,
          "maxLength": 1000
        }
      ]
    }
  ]
}
```

### Question Type 정의
| Type | Description |
|------|-------------|
| `text` | 한 줄 텍스트 입력 |
| `textarea` | 여러 줄 텍스트 입력 |
| `number` | 숫자 입력 |
| `select` | 단일 선택 (드롭다운) |
| `multiselect` | 복수 선택 |
| `date` | 날짜 선택 |
| `radio` | 라디오 버튼 |

## ✅ 구현 체크리스트
- [ ] WizardStepDefinition 엔티티 또는 설정 파일 구조 설계
- [ ] WizardController 또는 TemplateController 생성
- [ ] 단계/질문 정의 데이터 소스 결정 (DB vs JSON 파일)
- [ ] Response DTO 생성
- [ ] 단위 테스트 작성
- [ ] Swagger 문서화

## 🔗 사용처
- WizardStep 컴포넌트
- QuestionForm 컴포넌트

## 📎 관련 문서
- [BE-API-REQUEST.md](./BE-API-REQUEST.md)

