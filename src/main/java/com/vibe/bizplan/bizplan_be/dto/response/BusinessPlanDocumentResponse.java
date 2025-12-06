package com.vibe.bizplan.bizplan_be.dto.response;

import com.vibe.bizplan.bizplan_be.domain.entity.BusinessPlanDocument;
import com.vibe.bizplan.bizplan_be.domain.model.DocumentStatus;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 사업계획서 문서 응답 DTO.
 */
public record BusinessPlanDocumentResponse(
        
        /** 문서 ID */
        String documentId,
        
        /** 프로젝트 ID */
        String projectId,
        
        /** 버전 */
        int version,
        
        /** 상태 */
        DocumentStatus status,
        
        /** 섹션별 내용 */
        Map<String, String> sections,
        
        /** 전체 글자 수 */
        int totalWordCount,
        
        /** 생성 소요 시간 (ms) */
        int generationTimeMs,
        
        /** 생성일시 */
        LocalDateTime createdAt,
        
        /** 수정일시 */
        LocalDateTime updatedAt
) {
    
    /**
     * 엔티티에서 응답 DTO 생성.
     */
    public static BusinessPlanDocumentResponse from(BusinessPlanDocument document) {
        Map<String, String> sections = new LinkedHashMap<>();
        
        if (document.getExecutiveSummary() != null) {
            sections.put("executive_summary", document.getExecutiveSummary());
        }
        if (document.getProblemDefinition() != null) {
            sections.put("problem_definition", document.getProblemDefinition());
        }
        if (document.getSolution() != null) {
            sections.put("solution", document.getSolution());
        }
        if (document.getMarketAnalysis() != null) {
            sections.put("market_analysis", document.getMarketAnalysis());
        }
        if (document.getBusinessModel() != null) {
            sections.put("business_model", document.getBusinessModel());
        }
        if (document.getCompetitiveAnalysis() != null) {
            sections.put("competitive_analysis", document.getCompetitiveAnalysis());
        }
        if (document.getMarketingStrategy() != null) {
            sections.put("marketing_strategy", document.getMarketingStrategy());
        }
        if (document.getTeam() != null) {
            sections.put("team", document.getTeam());
        }
        if (document.getFinancialPlan() != null) {
            sections.put("financial_plan", document.getFinancialPlan());
        }
        if (document.getMilestones() != null) {
            sections.put("milestones", document.getMilestones());
        }
        
        return new BusinessPlanDocumentResponse(
                document.getId(),
                document.getProjectId(),
                document.getVersion(),
                document.getStatus(),
                sections,
                document.getTotalWordCount(),
                document.getGenerationTimeMs(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}

