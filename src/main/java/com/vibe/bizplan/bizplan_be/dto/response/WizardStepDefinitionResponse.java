package com.vibe.bizplan.bizplan_be.dto.response;

import java.util.List;

/**
 * 위저드 단계 정의 응답 DTO.
 * 위저드의 개별 단계 정의 정보를 담는다.
 */
public record WizardStepDefinitionResponse(
        /** 단계 ID */
        int id,
        
        /** 단계 제목 */
        String title,
        
        /** 단계 설명 */
        String description,
        
        /** 해당 단계의 질문 목록 */
        List<WizardQuestionResponse> questions
) {
}

