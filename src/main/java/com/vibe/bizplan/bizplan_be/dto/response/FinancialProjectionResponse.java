package com.vibe.bizplan.bizplan_be.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * 재무 추정 결과 응답 DTO.
 * 3년치 월별/연별 재무 데이터와 유닛 이코노믹스 지표.
 */
public record FinancialProjectionResponse(
        
        /** 프로젝트 ID */
        String projectId,
        
        /** 월별 손익 데이터 */
        List<MonthlyPL> monthlyPL,
        
        /** 연간 요약 데이터 */
        List<YearlySummary> yearlySummary,
        
        /** 유닛 이코노믹스 지표 */
        UnitEconomics unitEconomics
) {
    
    /**
     * 월별 손익 데이터.
     */
    public record MonthlyPL(
            /** 월차 (1, 2, 3, ...) */
            int month,
            
            /** 연도 (1, 2, 3) */
            int year,
            
            /** 활성 고객 수 */
            int activeCustomers,
            
            /** 신규 고객 수 */
            int newCustomers,
            
            /** 이탈 고객 수 */
            int churnedCustomers,
            
            /** 월 매출 */
            BigDecimal revenue,
            
            /** 변동비 */
            BigDecimal variableCosts,
            
            /** 고정비 */
            BigDecimal fixedCosts,
            
            /** 마케팅비 */
            BigDecimal marketingCosts,
            
            /** 영업이익 */
            BigDecimal operatingProfit,
            
            /** 누적 현금 */
            BigDecimal cumulativeCash
    ) {}
    
    /**
     * 연간 요약 데이터.
     */
    public record YearlySummary(
            /** 연도 (1, 2, 3) */
            int year,
            
            /** 연간 총 매출 */
            BigDecimal totalRevenue,
            
            /** 연간 총 비용 */
            BigDecimal totalCosts,
            
            /** 연간 영업이익 */
            BigDecimal operatingProfit,
            
            /** 연말 활성 고객 수 */
            int endCustomers,
            
            /** 연간 성장률 (전년 대비, %) */
            BigDecimal growthRate
    ) {}
    
    /**
     * 유닛 이코노믹스 지표.
     */
    public record UnitEconomics(
            /** 고객 획득 비용 (CAC) */
            BigDecimal cac,
            
            /** 고객 생애 가치 (LTV) */
            BigDecimal ltv,
            
            /** LTV/CAC 비율 */
            BigDecimal ltvCacRatio,
            
            /** 손익분기점 도달 월 (BEP Month, 미도달 시 -1) */
            int breakEvenMonth,
            
            /** 손익분기 고객 수 */
            int breakEvenCustomers,
            
            /** 월 평균 이탈률 */
            BigDecimal monthlyChurnRate,
            
            /** 고객 평균 유지 기간 (개월) */
            BigDecimal averageCustomerLifespan,
            
            /** 월 순증 고객 수 (평균) */
            BigDecimal averageNetNewCustomers
    ) {}
}

