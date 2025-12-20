package com.vibe.bizplan.bizplan_be.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * AI Engine PMF 진단 응답 DTO.
 */
public record PmfDiagnoseResponse(

        /** 프로젝트 ID */
        @JsonProperty("project_id")
        String projectId,

        /** 종합 점수 (0-100) */
        @JsonProperty("overall_score")
        int overallScore,

        /** 점수 등급 (excellent, good, fair, poor, critical) */
        @JsonProperty("score_grade")
        String scoreGrade,

        /** 카테고리별 점수 */
        @JsonProperty("category_scores")
        Map<String, Integer> categoryScores,

        /** 식별된 리스크 목록 */
        List<PmfRisk> risks,

        /** 강점 목록 */
        List<String> strengths,

        /** 종합 분석 요약 */
        String summary,

        /** 우선 조치 사항 목록 */
        @JsonProperty("priority_actions")
        List<String> priorityActions,

        /** 사용된 AI 모델 */
        @JsonProperty("model_used")
        String modelUsed,

        /** 분석 소요 시간 (ms) */
        @JsonProperty("analysis_time_ms")
        int analysisTimeMs
) {

    /**
     * PMF 리스크 정보.
     */
    public record PmfRisk(
            /** 카테고리 */
            String category,

            /** 리스크 제목 */
            String title,

            /** 리스크 설명 */
            String description,

            /** 위험 수준 (high, medium, low) */
            String level,

            /** 추천 조치 */
            String recommendation,

            /** 관련 섹션 */
            @JsonProperty("related_sections")
            List<String> relatedSections
    ) {}
}

