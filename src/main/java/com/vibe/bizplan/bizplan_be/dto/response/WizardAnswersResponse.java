package com.vibe.bizplan.bizplan_be.dto.response;

import java.util.Map;

import com.vibe.bizplan.bizplan_be.domain.model.BizPlanConstants;
import com.vibe.bizplan.bizplan_be.domain.model.TemplateCode;

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
     * 정적 팩토리 메서드 (템플릿 코드 기반).
     * 템플릿별로 정의된 전체 단계 수를 사용한다.
     *
     * @param projectId 프로젝트 ID
     * @param answers 답변 데이터
     * @param templateCode 템플릿 코드 (전체 단계 수 결정)
     * @return WizardAnswersResponse
     */
    public static WizardAnswersResponse of(String projectId, Map<String, Object> answers, TemplateCode templateCode) {
        int completedSteps = answers != null ? answers.size() : 0;
        int totalSteps = templateCode != null ? templateCode.getTotalSteps() : BizPlanConstants.DEFAULT_WIZARD_STEPS;
        return new WizardAnswersResponse(projectId, answers, completedSteps, totalSteps);
    }
}

