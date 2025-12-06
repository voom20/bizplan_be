package com.vibe.bizplan.bizplan_be.dto.request;

import java.math.BigDecimal;

import com.vibe.bizplan.bizplan_be.domain.model.BizPlanConstants;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 재무 추정 입력 변수 DTO.
 * 3년치 재무 추정을 위한 핵심 가정 변수들.
 * 
 * <h3>필수 파라미터 (Required)</h3>
 * <ul>
 *   <li>initialCapital - 초기 자본금</li>
 *   <li>averageRevenuePerUser - 월 평균 객단가 (ARPU)</li>
 *   <li>monthlyMarketingBudget - 월간 마케팅 예산</li>
 *   <li>customerAcquisitionCost - 고객 획득 비용 (CAC)</li>
 *   <li>monthlyChurnRate - 월간 이탈률</li>
 *   <li>monthlyFixedCosts - 월 고정비</li>
 * </ul>
 * 
 * <h3>선택 파라미터 (Optional with defaults)</h3>
 * <ul>
 *   <li>variableCostRate - 변동비율 (기본값: 0.3 = 30%)</li>
 *   <li>initialCustomers - 초기 고객 수 (기본값: 0)</li>
 *   <li>projectionMonths - 추정 기간 (기본값: 36개월)</li>
 * </ul>
 */
public record FinancialAssumptionsRequest(
        
        // ===== 필수 파라미터 (Required) =====
        
        /** 초기 자본금 (원) - 필수 */
        @NotNull(message = "초기 자본금은 필수입니다")
        @Min(value = 0, message = "초기 자본금은 0 이상이어야 합니다")
        BigDecimal initialCapital,
        
        /** 월 평균 객단가 (ARPU, 원) - 필수 */
        @NotNull(message = "월 평균 객단가는 필수입니다")
        @Min(value = 1, message = "객단가는 1 이상이어야 합니다")
        BigDecimal averageRevenuePerUser,
        
        /** 월간 마케팅 예산 (원) - 필수 */
        @NotNull(message = "월간 마케팅 예산은 필수입니다")
        @Min(value = 0, message = "마케팅 예산은 0 이상이어야 합니다")
        BigDecimal monthlyMarketingBudget,
        
        /** 고객 획득 비용 (CAC, 원) - 필수 */
        @NotNull(message = "CAC는 필수입니다")
        @Min(value = 1, message = "CAC는 1 이상이어야 합니다")
        BigDecimal customerAcquisitionCost,
        
        /** 월간 이탈률 (0.0 ~ 1.0, 예: 0.05 = 5%) - 필수 */
        @NotNull(message = "월간 이탈률은 필수입니다")
        @DecimalMin(value = "0.0", message = "이탈률은 0 이상이어야 합니다")
        @DecimalMax(value = "1.0", message = "이탈률은 1 이하여야 합니다")
        BigDecimal monthlyChurnRate,
        
        /** 월 고정비 (인건비 + 임대료 등, 원) - 필수 */
        @NotNull(message = "월 고정비는 필수입니다")
        @Min(value = 0, message = "고정비는 0 이상이어야 합니다")
        BigDecimal monthlyFixedCosts,
        
        // ===== 선택 파라미터 (Optional - 기본값 제공) =====
        
        /** 변동비율 (매출 대비, 0.0 ~ 1.0) - 선택, 기본값: 0.3 (30%) */
        @DecimalMin(value = "0.0", message = "변동비율은 0 이상이어야 합니다")
        @DecimalMax(value = "1.0", message = "변동비율은 1 이하여야 합니다")
        BigDecimal variableCostRate,
        
        /** 초기 고객 수 (기존 고객) - 선택, 기본값: 0 */
        @Min(value = 0, message = "초기 고객 수는 0 이상이어야 합니다")
        Integer initialCustomers,
        
        /** 추정 기간 (개월) - 선택, 기본값: 36개월 (3년) */
        @Min(value = 1, message = "추정 기간은 1개월 이상이어야 합니다")
        Integer projectionMonths
) {
    /**
     * Compact Constructor: 선택 파라미터에 기본값 적용.
     * null로 전달된 선택 파라미터는 아래 기본값으로 대체된다:
     * <ul>
     *   <li>variableCostRate: 0.3 (30%)</li>
     *   <li>initialCustomers: 0</li>
     *   <li>projectionMonths: 36 (3년)</li>
     * </ul>
     */
    public FinancialAssumptionsRequest {
        if (variableCostRate == null) {
            variableCostRate = BizPlanConstants.DEFAULT_VARIABLE_COST_RATE;
        }
        if (initialCustomers == null) {
            initialCustomers = 0;
        }
        if (projectionMonths == null) {
            projectionMonths = BizPlanConstants.DEFAULT_PROJECTION_MONTHS;
        }
    }
}

