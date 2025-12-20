package com.vibe.bizplan.bizplan_be.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * AI Engine PMF 진단 요청 DTO.
 */
public record PmfDiagnoseRequest(

        /** 프로젝트 ID */
        @JsonProperty("project_id")
        String projectId,

        /** 템플릿 코드 */
        @JsonProperty("template_code")
        String templateCode,

        /** 사업계획서 내용 (섹션별) */
        @JsonProperty("business_plan_content")
        Map<String, String> businessPlanContent,

        /** 재무 데이터 포함 여부 */
        @JsonProperty("include_financial_data")
        boolean includeFinancialData,

        /** 재무 데이터 (선택) */
        @JsonProperty("financial_data")
        Map<String, Object> financialData,

        /** Wizard 답변 컨텍스트 */
        Map<String, Object> context
) {

    /**
     * 간단 생성용 팩토리 메서드.
     */
    public static PmfDiagnoseRequest of(
            String projectId,
            String templateCode,
            Map<String, String> businessPlanContent,
            Map<String, Object> context
    ) {
        return new PmfDiagnoseRequest(
                projectId,
                templateCode,
                businessPlanContent,
                false,
                null,
                context
        );
    }

    /**
     * 재무 데이터 포함 생성용 팩토리 메서드.
     */
    public static PmfDiagnoseRequest withFinancialData(
            String projectId,
            String templateCode,
            Map<String, String> businessPlanContent,
            Map<String, Object> context,
            Map<String, Object> financialData
    ) {
        return new PmfDiagnoseRequest(
                projectId,
                templateCode,
                businessPlanContent,
                true,
                financialData,
                context
        );
    }
}

