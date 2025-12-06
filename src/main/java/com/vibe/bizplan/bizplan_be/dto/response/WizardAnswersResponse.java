package com.vibe.bizplan.bizplan_be.dto.response;

import java.util.Map;

/**
 * Wizard 답변 응답 DTO.
 * 저장된 Wizard 답변 데이터를 반환한다.
 */
public record WizardAnswersResponse(
        
        /** 프로젝트 ID */
        String projectId,
        
        /** 전체 Wizard 답변 데이터 (단계별 Key-Value) */
        Map<String, Object> answers,
        
        /** 완료된 단계 수 */
        int completedSteps,
        
        /** 전체 단계 수 */
        int totalSteps
) {
    
    /**
     * 정적 팩토리 메서드.
     *
     * @param projectId 프로젝트 ID
     * @param answers 답변 데이터
     * @return WizardAnswersResponse
     */
    public static WizardAnswersResponse of(String projectId, Map<String, Object> answers) {
        int completedSteps = answers != null ? answers.size() : 0;
        // TODO: 템플릿별 전체 단계 수 조회 로직 추가
        int totalSteps = 10; // 임시 기본값
        return new WizardAnswersResponse(projectId, answers, completedSteps, totalSteps);
    }
}

