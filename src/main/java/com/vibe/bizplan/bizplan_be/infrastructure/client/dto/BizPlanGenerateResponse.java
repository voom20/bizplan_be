package com.vibe.bizplan.bizplan_be.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * AI Engine 사업계획서 섹션 생성 응답 DTO.
 */
public record BizPlanGenerateResponse(
        
        /** 프로젝트 ID */
        @JsonProperty("project_id")
        String projectId,
        
        /** 생성된 섹션 */
        BizPlanSection section,
        
        /** 개선 제안 (Expert 모드) */
        List<String> suggestions,
        
        /** 일관성 경고 */
        @JsonProperty("consistency_warnings")
        List<String> consistencyWarnings
) {
    
    /**
     * 생성된 섹션 정보.
     */
    public record BizPlanSection(
            
            /** 섹션 타입 */
            @JsonProperty("section_type")
            String sectionType,
            
            /** 섹션 제목 */
            String title,
            
            /** 생성된 본문 */
            String content,
            
            /** 글자 수 */
            @JsonProperty("word_count")
            int wordCount,
            
            /** 사용된 모델 */
            @JsonProperty("model_used")
            String modelUsed,
            
            /** 생성 소요 시간 (ms) */
            @JsonProperty("generation_time_ms")
            int generationTimeMs,
            
            /** 토큰 사용량 */
            @JsonProperty("token_usage")
            Map<String, Integer> tokenUsage
    ) {}
}

