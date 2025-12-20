# [BE-API-006] PMF 설문 질문 조회 API 구현

## 📌 우선순위
🟡 **Low**

## 📋 요약
PMF(Product-Market Fit) 진단용 설문 질문 목록을 조회하는 API 엔드포인트 구현

## 🎯 요구사항

### 엔드포인트 (Option 1: 기본)
```
GET /pmf/questions
```

### 엔드포인트 (Option 2: 템플릿별)
```
GET /templates/{templateCode}/pmf/questions
```

### Response (200 OK)
```json
{
  "questions": [
    {
      "id": "pmf-1",
      "question": "만약 이 제품을 더 이상 사용할 수 없게 된다면 어떻게 느끼시겠습니까?",
      "type": "radio",
      "required": true,
      "options": [
        { "value": 1, "label": "매우 실망할 것이다" },
        { "value": 2, "label": "다소 실망할 것이다" },
        { "value": 3, "label": "실망하지 않을 것이다" },
        { "value": 4, "label": "해당 없음" }
      ]
    },
    {
      "id": "pmf-2",
      "question": "이 제품의 주요 혜택은 무엇입니까?",
      "type": "multiselect",
      "required": true,
      "options": [
        { "value": 1, "label": "시간 절약" },
        { "value": 2, "label": "비용 절감" },
        { "value": 3, "label": "품질 향상" },
        { "value": 4, "label": "기타" }
      ]
    }
  ]
}
```

## ✅ 구현 체크리스트
- [ ] PmfController 생성
- [ ] PMF 질문 데이터 소스 설계 (DB vs JSON 설정 파일)
- [ ] PmfQuestionResponse DTO 생성
- [ ] 단위 테스트 작성
- [ ] Swagger 문서화

## 🔗 사용처
- PMFSurvey 컴포넌트
- 위저드 5단계

## 📎 관련 문서
- [BE-API-REQUEST.md](./BE-API-REQUEST.md)

