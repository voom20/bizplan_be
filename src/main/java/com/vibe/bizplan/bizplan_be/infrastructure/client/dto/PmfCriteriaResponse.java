package com.vibe.bizplan.bizplan_be.infrastructure.client.dto;

import java.util.List;

/**
 * AI Engine PMF 평가 기준 응답 DTO.
 */
public record PmfCriteriaResponse(
        /** 평가 기준 목록 */
        List<CriteriaItem> criteria
) {
    /**
     * 평가 기준 항목.
     */
    public record CriteriaItem(
            /** 카테고리 코드 */
            String category,
            /** 기준 이름 */
            String name,
            /** 가중치 (0.0 ~ 1.0) */
            Double weight,
            /** 설명 */
            String description
    ) {}
}

