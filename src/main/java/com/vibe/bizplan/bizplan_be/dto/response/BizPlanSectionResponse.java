package com.vibe.bizplan.bizplan_be.dto.response;

import com.vibe.bizplan.bizplan_be.domain.entity.BizPlanSection;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사업계획서 섹션 응답 DTO.
 */
@Schema(description = "사업계획서 섹션 응답")
public record BizPlanSectionResponse(

        /** 프로젝트 ID */
        @Schema(description = "프로젝트 ID")
        String projectId,

        /** 섹션 타입 */
        @Schema(description = "섹션 타입", example = "executive_summary")
        String sectionType,

        /** 섹션 제목 */
        @Schema(description = "섹션 제목", example = "사업개요")
        String title,

        /** 섹션 내용 */
        @Schema(description = "섹션 내용")
        String content,

        /** 글자 수 */
        @Schema(description = "글자 수", example = "500")
        Integer wordCount,

        /** 사용된 모델 */
        @Schema(description = "생성에 사용된 AI 모델", example = "gemini-1.5-pro")
        String modelUsed,

        /** 생성 소요 시간 (ms) */
        @Schema(description = "AI 생성 소요 시간 (ms)", example = "3500")
        Integer generationTimeMs,

        /** 개선 제안 */
        @Schema(description = "AI의 개선 제안 (Expert 모드)")
        List<String> suggestions,

        /** 일관성 경고 */
        @Schema(description = "다른 섹션과의 일관성 경고")
        List<String> consistencyWarnings,

        /** 수정일시 */
        @Schema(description = "마지막 수정일시")
        LocalDateTime updatedAt
) {
    /**
     * 엔티티로부터 생성.
     */
    public static BizPlanSectionResponse from(BizPlanSection section) {
        return new BizPlanSectionResponse(
                section.getProjectId(),
                section.getSectionType(),
                section.getTitle(),
                section.getContent(),
                section.getWordCount(),
                section.getModelUsed(),
                section.getGenerationTimeMs(),
                null,
                null,
                section.getUpdatedAt()
        );
    }

    /**
     * AI 생성 결과로부터 생성 (제안 및 경고 포함).
     */
    public static BizPlanSectionResponse fromAiResult(
            BizPlanSection section,
            List<String> suggestions,
            List<String> consistencyWarnings
    ) {
        return new BizPlanSectionResponse(
                section.getProjectId(),
                section.getSectionType(),
                section.getTitle(),
                section.getContent(),
                section.getWordCount(),
                section.getModelUsed(),
                section.getGenerationTimeMs(),
                suggestions,
                consistencyWarnings,
                section.getUpdatedAt()
        );
    }
}

