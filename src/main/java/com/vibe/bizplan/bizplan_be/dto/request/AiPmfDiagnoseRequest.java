package com.vibe.bizplan.bizplan_be.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI PMF 진단 요청 DTO.
 * 클라이언트에서 AI 기반 PMF 진단을 요청할 때 사용.
 */
@Schema(description = "AI PMF 진단 요청")
public record AiPmfDiagnoseRequest(

        /** 재무 데이터 포함 여부 (기본값: true) */
        @Schema(description = "재무 데이터 포함 여부", example = "true", defaultValue = "true")
        Boolean includeFinancialData
) {
    /**
     * 기본값 적용 메서드.
     */
    public boolean isIncludeFinancialData() {
        return includeFinancialData == null || includeFinancialData;
    }
}

