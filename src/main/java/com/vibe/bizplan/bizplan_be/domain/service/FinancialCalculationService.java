package com.vibe.bizplan.bizplan_be.domain.service;

import com.vibe.bizplan.bizplan_be.dto.request.FinancialAssumptionsRequest;
import com.vibe.bizplan.bizplan_be.dto.response.FinancialProjectionResponse;
import com.vibe.bizplan.bizplan_be.dto.response.FinancialProjectionResponse.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 재무 추정 계산 서비스.
 * 핵심 변수를 기반으로 3년치 월별 손익과 유닛 이코노믹스를 계산한다.
 */
@Slf4j
@Service
public class FinancialCalculationService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * 재무 추정 생성.
     *
     * @param projectId 프로젝트 ID
     * @param assumptions 재무 가정 변수
     * @return 재무 추정 결과
     */
    public FinancialProjectionResponse generateProjection(String projectId, FinancialAssumptionsRequest assumptions) {
        log.info("재무 추정 시작: projectId={}, months={}", projectId, assumptions.projectionMonths());
        
        // 월별 손익 계산
        List<MonthlyPL> monthlyPLList = calculateMonthlyPL(assumptions);
        
        // 연간 요약 계산
        List<YearlySummary> yearlySummaryList = calculateYearlySummary(monthlyPLList);
        
        // 유닛 이코노믹스 계산
        UnitEconomics unitEconomics = calculateUnitEconomics(assumptions, monthlyPLList);
        
        log.info("재무 추정 완료: projectId={}", projectId);
        
        return new FinancialProjectionResponse(projectId, monthlyPLList, yearlySummaryList, unitEconomics);
    }

    /**
     * 월별 손익 계산.
     * 
     * 계산 로직:
     * - 신규 고객 = 마케팅 예산 / CAC
     * - 이탈 고객 = 전월 고객 * 이탈률
     * - 활성 고객 = 전월 고객 + 신규 고객 - 이탈 고객
     * - 매출 = 활성 고객 * 객단가
     * - 영업이익 = 매출 - (변동비 + 고정비 + 마케팅비)
     */
    private List<MonthlyPL> calculateMonthlyPL(FinancialAssumptionsRequest a) {
        List<MonthlyPL> result = new ArrayList<>();
        
        int activeCustomers = a.initialCustomers();
        BigDecimal cumulativeCash = a.initialCapital();
        
        // 월별 신규 고객 수 계산
        int monthlyNewCustomers = a.monthlyMarketingBudget()
                .divide(a.customerAcquisitionCost(), 0, RoundingMode.FLOOR)
                .intValue();
        
        for (int month = 1; month <= a.projectionMonths(); month++) {
            int year = (month - 1) / 12 + 1;
            
            // 이탈 고객 계산
            int churnedCustomers = BigDecimal.valueOf(activeCustomers)
                    .multiply(a.monthlyChurnRate())
                    .setScale(0, RoundingMode.FLOOR)
                    .intValue();
            
            // 신규 고객 (매월 동일)
            int newCustomers = monthlyNewCustomers;
            
            // 활성 고객 업데이트
            activeCustomers = activeCustomers + newCustomers - churnedCustomers;
            if (activeCustomers < 0) activeCustomers = 0;
            
            // 매출 계산
            BigDecimal revenue = a.averageRevenuePerUser()
                    .multiply(BigDecimal.valueOf(activeCustomers))
                    .setScale(SCALE, ROUNDING);
            
            // 변동비 계산
            BigDecimal variableCosts = revenue
                    .multiply(a.variableCostRate())
                    .setScale(SCALE, ROUNDING);
            
            // 고정비 (매월 동일)
            BigDecimal fixedCosts = a.monthlyFixedCosts();
            
            // 마케팅비 (매월 동일)
            BigDecimal marketingCosts = a.monthlyMarketingBudget();
            
            // 영업이익 계산
            BigDecimal operatingProfit = revenue
                    .subtract(variableCosts)
                    .subtract(fixedCosts)
                    .subtract(marketingCosts)
                    .setScale(SCALE, ROUNDING);
            
            // 누적 현금 업데이트
            cumulativeCash = cumulativeCash.add(operatingProfit).setScale(SCALE, ROUNDING);
            
            result.add(new MonthlyPL(
                    month, year, activeCustomers, newCustomers, churnedCustomers,
                    revenue, variableCosts, fixedCosts, marketingCosts,
                    operatingProfit, cumulativeCash
            ));
        }
        
        return result;
    }

    /**
     * 연간 요약 계산.
     */
    private List<YearlySummary> calculateYearlySummary(List<MonthlyPL> monthlyPLList) {
        List<YearlySummary> result = new ArrayList<>();
        
        int maxYear = monthlyPLList.stream()
                .mapToInt(MonthlyPL::year)
                .max()
                .orElse(1);
        
        BigDecimal previousYearRevenue = BigDecimal.ZERO;
        
        for (int year = 1; year <= maxYear; year++) {
            final int currentYear = year;
            
            List<MonthlyPL> yearData = monthlyPLList.stream()
                    .filter(m -> m.year() == currentYear)
                    .toList();
            
            if (yearData.isEmpty()) continue;
            
            BigDecimal totalRevenue = yearData.stream()
                    .map(MonthlyPL::revenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalCosts = yearData.stream()
                    .map(m -> m.variableCosts().add(m.fixedCosts()).add(m.marketingCosts()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal operatingProfit = totalRevenue.subtract(totalCosts);
            
            int endCustomers = yearData.get(yearData.size() - 1).activeCustomers();
            
            // 성장률 계산
            BigDecimal growthRate = BigDecimal.ZERO;
            if (previousYearRevenue.compareTo(BigDecimal.ZERO) > 0) {
                growthRate = totalRevenue.subtract(previousYearRevenue)
                        .divide(previousYearRevenue, 4, ROUNDING)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(SCALE, ROUNDING);
            }
            
            result.add(new YearlySummary(year, totalRevenue, totalCosts, operatingProfit, endCustomers, growthRate));
            
            previousYearRevenue = totalRevenue;
        }
        
        return result;
    }

    /**
     * 유닛 이코노믹스 계산.
     * 
     * - LTV = ARPU / 이탈률 (또는 ARPU * 평균 유지 기간)
     * - LTV/CAC 비율
     * - BEP (손익분기점) = 고정비를 충당할 수 있는 고객 수
     */
    private UnitEconomics calculateUnitEconomics(FinancialAssumptionsRequest a, List<MonthlyPL> monthlyPLList) {
        BigDecimal cac = a.customerAcquisitionCost();
        
        // 평균 고객 유지 기간 (개월) = 1 / 이탈률
        BigDecimal avgLifespan;
        if (a.monthlyChurnRate().compareTo(BigDecimal.ZERO) > 0) {
            avgLifespan = BigDecimal.ONE
                    .divide(a.monthlyChurnRate(), SCALE, ROUNDING);
        } else {
            avgLifespan = BigDecimal.valueOf(120); // 이탈률 0이면 10년으로 가정
        }
        
        // LTV = ARPU * 평균 유지 기간 * (1 - 변동비율)
        BigDecimal contributionMargin = BigDecimal.ONE.subtract(a.variableCostRate());
        BigDecimal ltv = a.averageRevenuePerUser()
                .multiply(avgLifespan)
                .multiply(contributionMargin)
                .setScale(SCALE, ROUNDING);
        
        // LTV/CAC 비율
        BigDecimal ltvCacRatio = ltv.divide(cac, SCALE, ROUNDING);
        
        // 손익분기점 (BEP) 월 찾기
        int bepMonth = -1;
        for (MonthlyPL m : monthlyPLList) {
            if (m.operatingProfit().compareTo(BigDecimal.ZERO) >= 0) {
                bepMonth = m.month();
                break;
            }
        }
        
        // 손익분기 고객 수 = 고정비 / (객단가 * 기여이익률 - CAC/평균유지기간)
        BigDecimal monthlyContribution = a.averageRevenuePerUser().multiply(contributionMargin);
        BigDecimal totalMonthlyFixed = a.monthlyFixedCosts().add(a.monthlyMarketingBudget());
        int bepCustomers;
        if (monthlyContribution.compareTo(BigDecimal.ZERO) > 0) {
            bepCustomers = totalMonthlyFixed
                    .divide(monthlyContribution, 0, RoundingMode.CEILING)
                    .intValue();
        } else {
            bepCustomers = Integer.MAX_VALUE;
        }
        
        // 평균 순증 고객 수
        BigDecimal avgNetNewCustomers = BigDecimal.ZERO;
        if (!monthlyPLList.isEmpty()) {
            int totalNetNew = monthlyPLList.stream()
                    .mapToInt(m -> m.newCustomers() - m.churnedCustomers())
                    .sum();
            avgNetNewCustomers = BigDecimal.valueOf(totalNetNew)
                    .divide(BigDecimal.valueOf(monthlyPLList.size()), SCALE, ROUNDING);
        }
        
        return new UnitEconomics(
                cac, ltv, ltvCacRatio, bepMonth, bepCustomers,
                a.monthlyChurnRate(), avgLifespan, avgNetNewCustomers
        );
    }
}

