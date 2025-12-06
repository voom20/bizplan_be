package com.vibe.bizplan.bizplan_be.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 재무 추정 입력 변수 DTO.
 * 3년치 재무 추정을 위한 핵심 가정 변수들.
 */
public record FinancialAssumptionsRequest(
        
        /** 초기 자본금 (원) */
        @NotNull(message = "초기 자본금은 필수입니다")
        @Min(value = 0, message = "초기 자본금은 0 이상이어야 합니다")
        BigDecimal initialCapital,
        
        /** 월 평균 객단가 (ARPU, 원) */
        @NotNull(message = "월 평균 객단가는 필수입니다")
        @Min(value = 1, message = "객단가는 1 이상이어야 합니다")
        BigDecimal averageRevenuePerUser,
        
        /** 월간 마케팅 예산 (원) */
        @NotNull(message = "월간 마케팅 예산은 필수입니다")
        @Min(value = 0, message = "마케팅 예산은 0 이상이어야 합니다")
        BigDecimal monthlyMarketingBudget,
        
        /** 고객 획득 비용 (CAC, 원) */
        @NotNull(message = "CAC는 필수입니다")
        @Min(value = 1, message = "CAC는 1 이상이어야 합니다")
        BigDecimal customerAcquisitionCost,
        
        /** 월간 이탈률 (0.0 ~ 1.0, 예: 0.05 = 5%) */
        @NotNull(message = "월간 이탈률은 필수입니다")
        @DecimalMin(value = "0.0", message = "이탈률은 0 이상이어야 합니다")
        @DecimalMax(value = "1.0", message = "이탈률은 1 이하여야 합니다")
        BigDecimal monthlyChurnRate,
        
        /** 월 고정비 (인건비 + 임대료 등, 원) */
        @NotNull(message = "월 고정비는 필수입니다")
        @Min(value = 0, message = "고정비는 0 이상이어야 합니다")
        BigDecimal monthlyFixedCosts,
        
        /** 변동비율 (매출 대비, 0.0 ~ 1.0, 예: 0.3 = 30%) */
        @DecimalMin(value = "0.0", message = "변동비율은 0 이상이어야 합니다")
        @DecimalMax(value = "1.0", message = "변동비율은 1 이하여야 합니다")
        BigDecimal variableCostRate,
        
        /** 초기 고객 수 (기존 고객) */
        @Min(value = 0, message = "초기 고객 수는 0 이상이어야 합니다")
        Integer initialCustomers,
        
        /** 추정 기간 (개월, 기본값: 36개월 = 3년) */
        @Min(value = 1, message = "추정 기간은 1개월 이상이어야 합니다")
        Integer projectionMonths
) {
    /**
     * 기본값 적용된 생성자.
     */
    public FinancialAssumptionsRequest {
        if (variableCostRate == null) {
            variableCostRate = BigDecimal.valueOf(0.3); // 기본 30%
        }
        if (initialCustomers == null) {
            initialCustomers = 0;
        }
        if (projectionMonths == null) {
            projectionMonths = 36; // 기본 3년
        }
    }
}

