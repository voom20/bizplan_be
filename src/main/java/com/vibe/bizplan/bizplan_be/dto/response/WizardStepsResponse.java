package com.vibe.bizplan.bizplan_be.dto.response;

import java.util.List;

/**
 * 위저드 단계 전체 정의 응답 DTO.
 * 템플릿별 위저드 단계 및 질문 정의 목록을 담는다.
 */
public record WizardStepsResponse(
        /** 템플릿 코드 */
        String templateCode,
        
        /** 총 단계 수 */
        int totalSteps,
        
        /** 단계 정의 목록 */
        List<WizardStepDefinitionResponse> steps
) {
}

