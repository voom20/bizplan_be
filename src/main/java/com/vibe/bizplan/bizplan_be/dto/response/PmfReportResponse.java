package com.vibe.bizplan.bizplan_be.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PMF 리포트 응답 DTO.
 */
public record PmfReportResponse(
        /** 프로젝트 ID */
        String projectId,
        
        /** PMF 점수 (0-100) */
        int score,
        
        /** PMF 레벨 (excellent, high, medium, low) */
        String level,
        
        /** 리스크 목록 */
        List<Risk> risks,
        
        /** 추천사항 목록 */
        List<Recommendation> recommendations,
        
        /** 설문 답변 목록 */
        List<PmfAnswer> answers,
        
        /** 생성일시 */
        LocalDateTime createdAt
) {
    /**
     * PMF 리스크.
     */
    public record Risk(
            String id,
            String title,
            String description,
            String severity  // high, medium, low
    ) {}
    
    /**
     * PMF 추천사항.
     */
    public record Recommendation(
            String id,
            String title,
            String description,
            String priority  // high, medium, low
    ) {}
    
    /**
     * PMF 설문 답변.
     */
    public record PmfAnswer(
            String questionId,
            Object value
    ) {}
}

