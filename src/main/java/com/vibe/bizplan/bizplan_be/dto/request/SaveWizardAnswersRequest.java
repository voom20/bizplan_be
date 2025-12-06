package com.vibe.bizplan.bizplan_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Wizard 단계별 답변 저장 요청 DTO.
 * 특정 단계의 답변을 저장하거나 업데이트한다.
 */
public record SaveWizardAnswersRequest(
        
        /** 단계 ID (예: "step1", "problem_definition") */
        @NotBlank(message = "단계 ID는 필수입니다")
        String stepId,
        
        /** 해당 단계의 답변 데이터 (Key-Value 형태) */
        @NotNull(message = "답변 데이터는 필수입니다")
        Map<String, Object> answers
) {
}

