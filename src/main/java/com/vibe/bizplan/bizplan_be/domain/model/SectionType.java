package com.vibe.bizplan.bizplan_be.domain.model;

import com.vibe.bizplan.bizplan_be.domain.entity.BusinessPlanDocument;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 사업계획서 섹션 타입 열거형.
 * 각 섹션에 대한 getter/setter를 캡슐화하여 switch 문 제거.
 */
@Getter
@RequiredArgsConstructor
public enum SectionType {
    
    EXECUTIVE_SUMMARY(
            "executive_summary",
            "요약",
            BusinessPlanDocument::getExecutiveSummary,
            BusinessPlanDocument::updateExecutiveSummary
    ),
    PROBLEM_DEFINITION(
            "problem_definition",
            "문제 정의",
            BusinessPlanDocument::getProblemDefinition,
            BusinessPlanDocument::updateProblemDefinition
    ),
    SOLUTION(
            "solution",
            "솔루션",
            BusinessPlanDocument::getSolution,
            BusinessPlanDocument::updateSolution
    ),
    MARKET_ANALYSIS(
            "market_analysis",
            "시장 분석",
            BusinessPlanDocument::getMarketAnalysis,
            BusinessPlanDocument::updateMarketAnalysis
    ),
    BUSINESS_MODEL(
            "business_model",
            "비즈니스 모델",
            BusinessPlanDocument::getBusinessModel,
            BusinessPlanDocument::updateBusinessModel
    ),
    COMPETITIVE_ANALYSIS(
            "competitive_analysis",
            "경쟁 분석",
            BusinessPlanDocument::getCompetitiveAnalysis,
            BusinessPlanDocument::updateCompetitiveAnalysis
    ),
    MARKETING_STRATEGY(
            "marketing_strategy",
            "마케팅 전략",
            BusinessPlanDocument::getMarketingStrategy,
            BusinessPlanDocument::updateMarketingStrategy
    ),
    TEAM(
            "team",
            "팀 소개",
            BusinessPlanDocument::getTeam,
            BusinessPlanDocument::updateTeam
    ),
    FINANCIAL_PLAN(
            "financial_plan",
            "재무 계획",
            BusinessPlanDocument::getFinancialPlan,
            BusinessPlanDocument::updateFinancialPlan
    ),
    MILESTONES(
            "milestones",
            "마일스톤",
            BusinessPlanDocument::getMilestones,
            BusinessPlanDocument::updateMilestones
    );

    /** 섹션 코드 (API에서 사용) */
    private final String code;
    
    /** 섹션 표시명 */
    private final String displayName;
    
    /** 섹션 내용 getter */
    private final Function<BusinessPlanDocument, String> getter;
    
    /** 섹션 내용 setter */
    private final BiConsumer<BusinessPlanDocument, String> setter;

    /**
     * 문서에서 해당 섹션 내용 조회.
     *
     * @param document 사업계획서 문서
     * @return 섹션 내용 (null 가능)
     */
    public String getContent(BusinessPlanDocument document) {
        return getter.apply(document);
    }

    /**
     * 문서의 해당 섹션 내용 업데이트.
     *
     * @param document 사업계획서 문서
     * @param content 새 내용
     */
    public void setContent(BusinessPlanDocument document, String content) {
        setter.accept(document, content);
    }

    /**
     * 코드로 SectionType 조회.
     *
     * @param code 섹션 코드
     * @return SectionType (Optional)
     */
    public static Optional<SectionType> fromCode(String code) {
        return Arrays.stream(values())
                .filter(s -> s.code.equals(code))
                .findFirst();
    }

    /**
     * 문서 생성 순서로 정렬된 섹션 목록.
     *
     * @return 순서대로 정렬된 SectionType 목록
     */
    public static List<SectionType> getOrderedSections() {
        return Arrays.asList(values());
    }
}

