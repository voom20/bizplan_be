package com.vibe.bizplan.bizplan_be.dto.response;

import com.vibe.bizplan.bizplan_be.infrastructure.client.dto.PmfDiagnoseResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI PMF 진단 응답 DTO.
 */
@Schema(description = "AI PMF 진단 응답")
public record AiPmfDiagnoseResponse(

        /** 프로젝트 ID */
        @Schema(description = "프로젝트 ID")
        String projectId,

        /** 종합 점수 (0-100) */
        @Schema(description = "종합 점수 (0-100)", example = "75")
        int overallScore,

        /** 점수 등급 */
        @Schema(description = "점수 등급", example = "good",
                allowableValues = {"excellent", "good", "fair", "poor", "critical"})
        String scoreGrade,

        /** 카테고리별 점수 */
        @Schema(description = "카테고리별 점수")
        Map<String, Integer> categoryScores,

        /** 식별된 리스크 목록 */
        @Schema(description = "식별된 리스크 목록")
        List<RiskItem> risks,

        /** 강점 목록 */
        @Schema(description = "강점 목록")
        List<String> strengths,

        /** 종합 분석 요약 */
        @Schema(description = "종합 분석 요약")
        String summary,

        /** 우선 조치 사항 목록 */
        @Schema(description = "우선 조치 사항 목록")
        List<String> priorityActions,

        /** 사용된 AI 모델 */
        @Schema(description = "사용된 AI 모델", example = "gemini-1.5-pro")
        String modelUsed,

        /** 분석 소요 시간 (ms) */
        @Schema(description = "분석 소요 시간 (ms)", example = "3500")
        int analysisTimeMs,

        /** 분석 일시 */
        @Schema(description = "분석 일시")
        LocalDateTime analyzedAt
) {

    /**
     * 리스크 항목 DTO.
     */
    @Schema(description = "리스크 항목")
    public record RiskItem(
            /** 카테고리 */
            @Schema(description = "카테고리", example = "market")
            String category,

            /** 리스크 제목 */
            @Schema(description = "리스크 제목", example = "시장 규모 불확실")
            String title,

            /** 리스크 설명 */
            @Schema(description = "리스크 설명")
            String description,

            /** 위험 수준 */
            @Schema(description = "위험 수준", example = "medium",
                    allowableValues = {"high", "medium", "low"})
            String level,

            /** 추천 조치 */
            @Schema(description = "추천 조치")
            String recommendation,

            /** 관련 섹션 */
            @Schema(description = "관련 섹션")
            List<String> relatedSections
    ) {}

    /**
     * AI Engine 응답으로부터 생성.
     */
    public static AiPmfDiagnoseResponse from(PmfDiagnoseResponse aiResponse) {
        List<RiskItem> risks = aiResponse.risks() != null
                ? aiResponse.risks().stream()
                        .map(r -> new RiskItem(
                                r.category(),
                                r.title(),
                                r.description(),
                                r.level(),
                                r.recommendation(),
                                r.relatedSections()
                        ))
                        .toList()
                : List.of();

        return new AiPmfDiagnoseResponse(
                aiResponse.projectId(),
                aiResponse.overallScore(),
                aiResponse.scoreGrade(),
                aiResponse.categoryScores(),
                risks,
                aiResponse.strengths() != null ? aiResponse.strengths() : List.of(),
                aiResponse.summary(),
                aiResponse.priorityActions() != null ? aiResponse.priorityActions() : List.of(),
                aiResponse.modelUsed(),
                aiResponse.analysisTimeMs(),
                LocalDateTime.now()
        );
    }
}

