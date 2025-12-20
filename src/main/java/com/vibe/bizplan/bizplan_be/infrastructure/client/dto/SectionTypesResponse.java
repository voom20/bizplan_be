package com.vibe.bizplan.bizplan_be.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * AI Engine 섹션 타입 목록 응답 DTO.
 */
public record SectionTypesResponse(
        /** 섹션 타입 목록 */
        @JsonProperty("section_types")
        List<SectionTypeInfo> sectionTypes
) {
    /**
     * 섹션 타입 정보.
     */
    public record SectionTypeInfo(
            /** 섹션 타입 코드 */
            String type,
            /** 섹션 이름 */
            String name,
            /** 섹션 설명 */
            String description
    ) {}
}

