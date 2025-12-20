package com.vibe.bizplan.bizplan_be.dto.response;

import com.vibe.bizplan.bizplan_be.infrastructure.client.dto.SectionTypesResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 사업계획서 섹션 타입 목록 응답 DTO.
 */
@Schema(description = "사업계획서 섹션 타입 목록 응답")
public record SectionTypesListResponse(

        /** 섹션 타입 목록 */
        @Schema(description = "섹션 타입 목록")
        List<SectionTypeItem> sectionTypes
) {

    /**
     * 섹션 타입 항목.
     */
    @Schema(description = "섹션 타입 항목")
    public record SectionTypeItem(
            /** 섹션 타입 코드 */
            @Schema(description = "섹션 타입 코드", example = "executive_summary")
            String type,

            /** 섹션 이름 */
            @Schema(description = "섹션 이름", example = "사업개요")
            String name,

            /** 섹션 설명 */
            @Schema(description = "섹션 설명", example = "사업의 핵심 내용을 요약합니다.")
            String description
    ) {}

    /**
     * AI Engine 응답으로부터 생성.
     */
    public static SectionTypesListResponse from(SectionTypesResponse aiResponse) {
        List<SectionTypeItem> items = aiResponse != null && aiResponse.sectionTypes() != null
                ? aiResponse.sectionTypes().stream()
                        .map(s -> new SectionTypeItem(s.type(), s.name(), s.description()))
                        .toList()
                : List.of();

        return new SectionTypesListResponse(items);
    }
}

