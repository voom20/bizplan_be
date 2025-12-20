package com.vibe.bizplan.bizplan_be.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 사업계획서 섹션 목록 응답 DTO.
 */
@Schema(description = "사업계획서 섹션 목록 응답")
public record BizPlanSectionsListResponse(

        /** 프로젝트 ID */
        @Schema(description = "프로젝트 ID")
        String projectId,

        /** 섹션 목록 */
        @Schema(description = "섹션 목록")
        List<SectionSummary> sections,

        /** 총 섹션 수 */
        @Schema(description = "총 섹션 수", example = "10")
        int totalSections,

        /** 완료된 섹션 수 */
        @Schema(description = "완료된 섹션 수", example = "3")
        int completedSections
) {
    /**
     * 섹션 요약 정보.
     */
    @Schema(description = "섹션 요약 정보")
    public record SectionSummary(
            /** 섹션 타입 */
            @Schema(description = "섹션 타입", example = "executive_summary")
            String sectionType,

            /** 섹션 제목 */
            @Schema(description = "섹션 제목", example = "사업개요")
            String title,

            /** 글자 수 */
            @Schema(description = "글자 수", example = "500")
            Integer wordCount,

            /** 수정일시 */
            @Schema(description = "마지막 수정일시")
            java.time.LocalDateTime updatedAt
    ) {}

    /**
     * 팩토리 메서드.
     */
    public static BizPlanSectionsListResponse of(
            String projectId,
            List<SectionSummary> sections
    ) {
        // 지원되는 전체 섹션 수 (10개)
        int totalSections = 10;
        int completedSections = sections.size();
        return new BizPlanSectionsListResponse(projectId, sections, totalSections, completedSections);
    }
}

