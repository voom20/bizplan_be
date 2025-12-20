package com.vibe.bizplan.bizplan_be.dto.response;

import com.vibe.bizplan.bizplan_be.infrastructure.client.dto.PmfCriteriaResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * PMF 평가 기준 목록 응답 DTO.
 */
@Schema(description = "PMF 평가 기준 목록 응답")
public record PmfCriteriaListResponse(

        /** 평가 기준 목록 */
        @Schema(description = "평가 기준 목록")
        List<CriteriaItem> criteria
) {

    /**
     * 평가 기준 항목.
     */
    @Schema(description = "평가 기준 항목")
    public record CriteriaItem(
            /** 카테고리 코드 */
            @Schema(description = "카테고리 코드", example = "market_fit")
            String category,

            /** 기준 이름 */
            @Schema(description = "기준 이름", example = "시장 적합성")
            String name,

            /** 가중치 (0.0 ~ 1.0) */
            @Schema(description = "가중치 (0.0 ~ 1.0)", example = "0.3")
            Double weight,

            /** 설명 */
            @Schema(description = "설명", example = "목표 시장에 대한 적합성 평가")
            String description
    ) {}

    /**
     * AI Engine 응답으로부터 생성.
     */
    public static PmfCriteriaListResponse from(PmfCriteriaResponse aiResponse) {
        List<CriteriaItem> items = aiResponse != null && aiResponse.criteria() != null
                ? aiResponse.criteria().stream()
                        .map(c -> new CriteriaItem(c.category(), c.name(), c.weight(), c.description()))
                        .toList()
                : List.of();

        return new PmfCriteriaListResponse(items);
    }
}

