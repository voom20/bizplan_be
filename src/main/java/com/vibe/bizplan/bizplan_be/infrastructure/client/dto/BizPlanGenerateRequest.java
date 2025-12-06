package com.vibe.bizplan.bizplan_be.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * AI Engine 사업계획서 섹션 생성 요청 DTO.
 */
public record BizPlanGenerateRequest(
        
        /** 프로젝트 ID */
        @JsonProperty("project_id")
        String projectId,
        
        /** 템플릿 코드 */
        @JsonProperty("template_code")
        String templateCode,
        
        /** 생성할 섹션 타입 */
        @JsonProperty("section_type")
        String sectionType,
        
        /** 생성 모드 (easy/expert) */
        String mode,
        
        /** Wizard 답변 컨텍스트 */
        Map<String, Object> context,
        
        /** 이전 섹션 내용 (일관성 유지용) */
        @JsonProperty("previous_sections")
        Map<String, String> previousSections,
        
        /** 추가 지시사항 */
        @JsonProperty("additional_instructions")
        String additionalInstructions
) {
    
    /**
     * 간단 생성용 팩토리 메서드.
     */
    public static BizPlanGenerateRequest of(
            String projectId,
            String templateCode,
            String sectionType,
            Map<String, Object> context
    ) {
        return new BizPlanGenerateRequest(
                projectId,
                templateCode,
                sectionType,
                "easy",
                context,
                null,
                null
        );
    }
    
    /**
     * 이전 섹션 포함 생성용 팩토리 메서드.
     */
    public static BizPlanGenerateRequest withPreviousSections(
            String projectId,
            String templateCode,
            String sectionType,
            Map<String, Object> context,
            Map<String, String> previousSections
    ) {
        return new BizPlanGenerateRequest(
                projectId,
                templateCode,
                sectionType,
                "easy",
                context,
                previousSections,
                null
        );
    }
}

