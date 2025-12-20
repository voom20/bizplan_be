package com.vibe.bizplan.bizplan_be.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.bizplan.bizplan_be.dto.request.FinancialAssumptionsRequest;
import com.vibe.bizplan.bizplan_be.dto.response.FinancialProjectionResponse;
import com.vibe.bizplan.bizplan_be.dto.response.FinancialProjectionResponse.MonthlyPL;
import com.vibe.bizplan.bizplan_be.dto.response.FinancialProjectionResponse.UnitEconomics;
import com.vibe.bizplan.bizplan_be.dto.response.FinancialProjectionResponse.YearlySummary;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.FinancialDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * FinancialCalculationService 단위 테스트.
 * 재무 추정 계산 로직을 검증한다.
 * 
 * 핵심 검증 항목:
 * - 월별 손익 계산 정확성
 * - 연간 요약 집계
 * - 유닛 이코노믹스 (LTV, CAC, BEP) 계산
 */
@DisplayName("FinancialCalculationService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class FinancialCalculationServiceTest {

    @Mock
    private FinancialDataRepository financialDataRepository;

    private FinancialCalculationService financialCalculationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        financialCalculationService = new FinancialCalculationService(financialDataRepository, objectMapper);
        
        // Mock repository save behavior
        when(financialDataRepository.findByProjectId(any())).thenReturn(Optional.empty());
        when(financialDataRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /**
     * 기본 재무 가정 생성 (테스트용).
     * - 초기 자본금: 1억원
     * - 월 객단가: 10만원
     * - 월 마케팅비: 500만원
     * - CAC: 5만원 (신규 고객 100명/월)
     * - 월 이탈률: 5%
     * - 월 고정비: 300만원
     * - 변동비율: 30%
     */
    private FinancialAssumptionsRequest createDefaultAssumptions() {
        return new FinancialAssumptionsRequest(
                BigDecimal.valueOf(100_000_000),  // initialCapital
                BigDecimal.valueOf(100_000),     // averageRevenuePerUser
                BigDecimal.valueOf(5_000_000),   // monthlyMarketingBudget
                BigDecimal.valueOf(50_000),      // customerAcquisitionCost
                BigDecimal.valueOf(0.05),        // monthlyChurnRate
                BigDecimal.valueOf(3_000_000),   // monthlyFixedCosts
                BigDecimal.valueOf(0.3),         // variableCostRate
                0,                                // initialCustomers
                36                                // projectionMonths
        );
    }

    @Nested
    @DisplayName("generateProjection 메서드")
    class GenerateProjectionTest {

        @Test
        @DisplayName("36개월 재무 추정 생성 성공")
        void generateProjection_36Months_ReturnsCompleteProjection() {
            // given
            FinancialAssumptionsRequest assumptions = createDefaultAssumptions();

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then
            assertThat(result).isNotNull();
            assertThat(result.projectId()).isEqualTo("test-project-id");
            assertThat(result.monthlyPL()).hasSize(36);
            assertThat(result.yearlySummary()).hasSize(3);
            assertThat(result.unitEconomics()).isNotNull();
        }

        @Test
        @DisplayName("12개월 재무 추정 생성 성공")
        void generateProjection_12Months_ReturnsOneYearProjection() {
            // given
            FinancialAssumptionsRequest assumptions = new FinancialAssumptionsRequest(
                    BigDecimal.valueOf(50_000_000),
                    BigDecimal.valueOf(100_000),
                    BigDecimal.valueOf(2_000_000),
                    BigDecimal.valueOf(40_000),
                    BigDecimal.valueOf(0.05),
                    BigDecimal.valueOf(2_000_000),
                    BigDecimal.valueOf(0.3),
                    0,
                    12
            );

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then
            assertThat(result.monthlyPL()).hasSize(12);
            assertThat(result.yearlySummary()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("월별 손익 계산 테스트")
    class MonthlyPLCalculationTest {

        @Test
        @DisplayName("첫 달 신규 고객 수 계산 정확성 검증")
        void monthlyPL_FirstMonth_CorrectNewCustomers() {
            // given: 마케팅비 500만원 / CAC 5만원 = 100명
            FinancialAssumptionsRequest assumptions = createDefaultAssumptions();

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then
            MonthlyPL firstMonth = result.monthlyPL().get(0);
            assertThat(firstMonth.month()).isEqualTo(1);
            assertThat(firstMonth.newCustomers()).isEqualTo(100);
        }

        @Test
        @DisplayName("매출 계산 정확성 검증 (활성고객 * 객단가)")
        void monthlyPL_Revenue_EqualsCustomersTimesARPU() {
            // given
            FinancialAssumptionsRequest assumptions = createDefaultAssumptions();

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then
            MonthlyPL firstMonth = result.monthlyPL().get(0);
            BigDecimal expectedRevenue = BigDecimal.valueOf(100_000L * firstMonth.activeCustomers());
            assertThat(firstMonth.revenue()).isEqualByComparingTo(expectedRevenue);
        }

        @Test
        @DisplayName("변동비 계산 정확성 검증 (매출 * 변동비율)")
        void monthlyPL_VariableCosts_EqualsRevenueTimesRate() {
            // given
            FinancialAssumptionsRequest assumptions = createDefaultAssumptions();

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then
            MonthlyPL firstMonth = result.monthlyPL().get(0);
            BigDecimal expectedVariableCosts = firstMonth.revenue()
                    .multiply(BigDecimal.valueOf(0.3))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            assertThat(firstMonth.variableCosts()).isEqualByComparingTo(expectedVariableCosts);
        }

        @Test
        @DisplayName("영업이익 계산 정확성 검증")
        void monthlyPL_OperatingProfit_CorrectCalculation() {
            // given
            FinancialAssumptionsRequest assumptions = createDefaultAssumptions();

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then
            MonthlyPL firstMonth = result.monthlyPL().get(0);
            BigDecimal expectedProfit = firstMonth.revenue()
                    .subtract(firstMonth.variableCosts())
                    .subtract(firstMonth.fixedCosts())
                    .subtract(firstMonth.marketingCosts());
            assertThat(firstMonth.operatingProfit()).isEqualByComparingTo(expectedProfit);
        }

        @Test
        @DisplayName("월별 고객 누적 증가 검증")
        void monthlyPL_CustomerGrowth_IncreasesOverTime() {
            // given
            FinancialAssumptionsRequest assumptions = createDefaultAssumptions();

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then: 순증 고객이 양수이면 시간이 지남에 따라 고객 증가
            List<MonthlyPL> monthlyPL = result.monthlyPL();
            MonthlyPL firstMonth = monthlyPL.get(0);
            MonthlyPL lastMonth = monthlyPL.get(monthlyPL.size() - 1);
            
            // 신규고객(100) > 이탈고객(초기 0에서 시작하므로 처음엔 0)이면 고객 증가
            assertThat(lastMonth.activeCustomers()).isGreaterThan(firstMonth.activeCustomers());
        }

        @Test
        @DisplayName("이탈 고객 계산 검증 (전월 고객 * 이탈률)")
        void monthlyPL_ChurnedCustomers_CorrectCalculation() {
            // given: 초기 고객 200명, 이탈률 5%
            FinancialAssumptionsRequest assumptions = new FinancialAssumptionsRequest(
                    BigDecimal.valueOf(100_000_000),
                    BigDecimal.valueOf(100_000),
                    BigDecimal.valueOf(5_000_000),
                    BigDecimal.valueOf(50_000),
                    BigDecimal.valueOf(0.05),  // 5% 이탈률
                    BigDecimal.valueOf(3_000_000),
                    BigDecimal.valueOf(0.3),
                    200,  // 초기 고객 200명
                    12
            );

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then: 첫 달 이탈 고객 = 200 * 0.05 = 10명
            MonthlyPL firstMonth = result.monthlyPL().get(0);
            assertThat(firstMonth.churnedCustomers()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("연간 요약 계산 테스트")
    class YearlySummaryCalculationTest {

        @Test
        @DisplayName("연간 매출 합계 정확성 검증")
        void yearlySummary_TotalRevenue_SumOfMonthlyRevenue() {
            // given
            FinancialAssumptionsRequest assumptions = createDefaultAssumptions();

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then
            YearlySummary year1 = result.yearlySummary().get(0);
            BigDecimal expectedYear1Revenue = result.monthlyPL().stream()
                    .filter(m -> m.year() == 1)
                    .map(MonthlyPL::revenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            assertThat(year1.totalRevenue()).isEqualByComparingTo(expectedYear1Revenue);
        }

        @Test
        @DisplayName("3개년 요약 데이터 생성 확인")
        void yearlySummary_ThreeYears_AllYearsPresent() {
            // given
            FinancialAssumptionsRequest assumptions = createDefaultAssumptions();

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then
            List<YearlySummary> summaries = result.yearlySummary();
            assertThat(summaries).hasSize(3);
            assertThat(summaries.get(0).year()).isEqualTo(1);
            assertThat(summaries.get(1).year()).isEqualTo(2);
            assertThat(summaries.get(2).year()).isEqualTo(3);
        }

        @Test
        @DisplayName("연말 고객 수 정확성 검증")
        void yearlySummary_EndCustomers_EqualsLastMonthOfYear() {
            // given
            FinancialAssumptionsRequest assumptions = createDefaultAssumptions();

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then
            YearlySummary year1 = result.yearlySummary().get(0);
            MonthlyPL month12 = result.monthlyPL().get(11); // 12번째 월
            
            assertThat(year1.endCustomers()).isEqualTo(month12.activeCustomers());
        }

        @Test
        @DisplayName("2년차 성장률 계산 검증")
        void yearlySummary_GrowthRate_CalculatedCorrectly() {
            // given
            FinancialAssumptionsRequest assumptions = createDefaultAssumptions();

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then
            YearlySummary year1 = result.yearlySummary().get(0);
            YearlySummary year2 = result.yearlySummary().get(1);
            
            // 1년차 성장률은 0 (이전 데이터 없음)
            assertThat(year1.growthRate()).isEqualByComparingTo(BigDecimal.ZERO);
            
            // 2년차 성장률 = (2년차 매출 - 1년차 매출) / 1년차 매출 * 100
            if (year1.totalRevenue().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal expectedGrowthRate = year2.totalRevenue()
                        .subtract(year1.totalRevenue())
                        .divide(year1.totalRevenue(), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, java.math.RoundingMode.HALF_UP);
                assertThat(year2.growthRate()).isEqualByComparingTo(expectedGrowthRate);
            }
        }
    }

    @Nested
    @DisplayName("유닛 이코노믹스 계산 테스트")
    class UnitEconomicsCalculationTest {

        @Test
        @DisplayName("CAC 값 정확성 검증")
        void unitEconomics_CAC_EqualsInput() {
            // given
            FinancialAssumptionsRequest assumptions = createDefaultAssumptions();

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then
            UnitEconomics unitEconomics = result.unitEconomics();
            assertThat(unitEconomics.cac()).isEqualByComparingTo(BigDecimal.valueOf(50_000));
        }

        @Test
        @DisplayName("LTV/CAC 비율 계산 검증")
        void unitEconomics_LtvCacRatio_CalculatedCorrectly() {
            // given
            FinancialAssumptionsRequest assumptions = createDefaultAssumptions();

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then
            UnitEconomics unitEconomics = result.unitEconomics();
            BigDecimal expectedRatio = unitEconomics.ltv()
                    .divide(unitEconomics.cac(), 2, java.math.RoundingMode.HALF_UP);
            assertThat(unitEconomics.ltvCacRatio()).isEqualByComparingTo(expectedRatio);
        }

        @Test
        @DisplayName("평균 고객 유지 기간 계산 검증 (1/이탈률)")
        void unitEconomics_AverageLifespan_EqualsInverseOfChurnRate() {
            // given: 이탈률 5% = 0.05
            FinancialAssumptionsRequest assumptions = createDefaultAssumptions();

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then: 평균 유지 기간 = 1 / 0.05 = 20개월
            UnitEconomics unitEconomics = result.unitEconomics();
            assertThat(unitEconomics.averageCustomerLifespan()).isEqualByComparingTo(BigDecimal.valueOf(20));
        }

        @Test
        @DisplayName("월간 이탈률 값 정확성 검증")
        void unitEconomics_MonthlyChurnRate_EqualsInput() {
            // given
            FinancialAssumptionsRequest assumptions = createDefaultAssumptions();

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then
            UnitEconomics unitEconomics = result.unitEconomics();
            assertThat(unitEconomics.monthlyChurnRate()).isEqualByComparingTo(BigDecimal.valueOf(0.05));
        }

        @Test
        @DisplayName("손익분기점(BEP) 도달 확인")
        void unitEconomics_BreakEvenMonth_ReachedEventually() {
            // given: 수익성 있는 비즈니스 모델
            FinancialAssumptionsRequest assumptions = new FinancialAssumptionsRequest(
                    BigDecimal.valueOf(100_000_000),
                    BigDecimal.valueOf(200_000),     // 높은 객단가
                    BigDecimal.valueOf(3_000_000),   // 낮은 마케팅비
                    BigDecimal.valueOf(30_000),      // 낮은 CAC
                    BigDecimal.valueOf(0.03),        // 낮은 이탈률
                    BigDecimal.valueOf(2_000_000),   // 낮은 고정비
                    BigDecimal.valueOf(0.2),         // 낮은 변동비율
                    50,                               // 초기 고객 50명
                    36
            );

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then: BEP 도달 (양수 영업이익 달성 월)
            UnitEconomics unitEconomics = result.unitEconomics();
            assertThat(unitEconomics.breakEvenMonth()).isGreaterThan(0);
        }

        @Test
        @DisplayName("이탈률 0인 경우 평균 유지 기간 최대값 검증")
        void unitEconomics_ZeroChurnRate_MaxLifespan() {
            // given: 이탈률 0
            FinancialAssumptionsRequest assumptions = new FinancialAssumptionsRequest(
                    BigDecimal.valueOf(100_000_000),
                    BigDecimal.valueOf(100_000),
                    BigDecimal.valueOf(5_000_000),
                    BigDecimal.valueOf(50_000),
                    BigDecimal.ZERO,  // 이탈률 0
                    BigDecimal.valueOf(3_000_000),
                    BigDecimal.valueOf(0.3),
                    0,
                    12
            );

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then: 평균 유지 기간 = 120개월 (10년 최대값)
            UnitEconomics unitEconomics = result.unitEconomics();
            assertThat(unitEconomics.averageCustomerLifespan()).isEqualByComparingTo(BigDecimal.valueOf(120));
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCasesTest {

        @Test
        @DisplayName("초기 고객이 있는 경우 첫 달 매출 발생")
        void withInitialCustomers_FirstMonthHasRevenue() {
            // given
            FinancialAssumptionsRequest assumptions = new FinancialAssumptionsRequest(
                    BigDecimal.valueOf(50_000_000),
                    BigDecimal.valueOf(100_000),
                    BigDecimal.valueOf(2_000_000),
                    BigDecimal.valueOf(40_000),
                    BigDecimal.valueOf(0.05),
                    BigDecimal.valueOf(2_000_000),
                    BigDecimal.valueOf(0.3),
                    100,  // 초기 고객 100명
                    12
            );

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then
            MonthlyPL firstMonth = result.monthlyPL().get(0);
            assertThat(firstMonth.activeCustomers()).isGreaterThanOrEqualTo(100);
            assertThat(firstMonth.revenue()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("기본값이 적용된 선택 파라미터 테스트")
        void withNullOptionalParams_DefaultsApplied() {
            // given: 선택 파라미터를 null로 전달
            FinancialAssumptionsRequest assumptions = new FinancialAssumptionsRequest(
                    BigDecimal.valueOf(100_000_000),
                    BigDecimal.valueOf(100_000),
                    BigDecimal.valueOf(5_000_000),
                    BigDecimal.valueOf(50_000),
                    BigDecimal.valueOf(0.05),
                    BigDecimal.valueOf(3_000_000),
                    null,  // 기본값: 0.3
                    null,  // 기본값: 0
                    null   // 기본값: 36
            );

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then
            assertThat(result.monthlyPL()).hasSize(36);
        }

        @Test
        @DisplayName("누적 현금이 정확히 계산되는지 검증")
        void cumulativeCash_AccumulatesCorrectly() {
            // given
            FinancialAssumptionsRequest assumptions = createDefaultAssumptions();

            // when
            FinancialProjectionResponse result = financialCalculationService.generateProjection(
                    "test-project-id", assumptions);

            // then: 누적 현금 = 초기 자본 + 누적 영업이익
            BigDecimal initialCapital = BigDecimal.valueOf(100_000_000);
            BigDecimal cumulativeProfit = BigDecimal.ZERO;
            
            for (MonthlyPL month : result.monthlyPL()) {
                cumulativeProfit = cumulativeProfit.add(month.operatingProfit());
                BigDecimal expectedCash = initialCapital.add(cumulativeProfit);
                assertThat(month.cumulativeCash()).isEqualByComparingTo(expectedCash);
            }
        }
    }
}

