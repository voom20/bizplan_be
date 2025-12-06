package com.vibe.bizplan.bizplan_be.domain.entity;

import com.vibe.bizplan.bizplan_be.domain.model.DocumentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사업계획서 문서 엔티티.
 * AI 엔진에서 생성된 사업계획서 섹션별 내용을 저장한다.
 */
@Entity
@Table(name = "business_plan_documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BusinessPlanDocument {

    @Id
    @Column(length = 36)
    private String id;

    /** 연결된 프로젝트 ID */
    @Column(name = "project_id", nullable = false, length = 36)
    private String projectId;

    /** 문서 버전 (동일 프로젝트 내 여러 버전 가능) */
    @Column(nullable = false)
    private int version;

    /** 문서 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;

    // ==========================================
    // 섹션별 내용
    // ==========================================
    
    /** 요약 */
    @Column(columnDefinition = "TEXT")
    private String executiveSummary;

    /** 문제 정의 */
    @Column(columnDefinition = "TEXT")
    private String problemDefinition;

    /** 솔루션 */
    @Column(columnDefinition = "TEXT")
    private String solution;

    /** 시장 분석 */
    @Column(columnDefinition = "TEXT")
    private String marketAnalysis;

    /** 비즈니스 모델 */
    @Column(columnDefinition = "TEXT")
    private String businessModel;

    /** 경쟁 분석 */
    @Column(columnDefinition = "TEXT")
    private String competitiveAnalysis;

    /** 마케팅 전략 */
    @Column(columnDefinition = "TEXT")
    private String marketingStrategy;

    /** 팀 소개 */
    @Column(columnDefinition = "TEXT")
    private String team;

    /** 재무 계획 */
    @Column(columnDefinition = "TEXT")
    private String financialPlan;

    /** 마일스톤 */
    @Column(columnDefinition = "TEXT")
    private String milestones;

    // ==========================================
    // 메타데이터
    // ==========================================
    
    /** 전체 글자 수 */
    @Column(name = "total_word_count")
    private int totalWordCount;

    /** 생성 소요 시간 (ms) */
    @Column(name = "generation_time_ms")
    private int generationTimeMs;

    /** 생성일시 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정일시 */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==========================================
    // 정적 팩토리 메서드
    // ==========================================

    /**
     * 새 문서 생성 (생성 중 상태).
     *
     * @param projectId 프로젝트 ID
     * @param version 버전 번호
     * @return 새 BusinessPlanDocument 인스턴스
     */
    public static BusinessPlanDocument createNew(String projectId, int version) {
        BusinessPlanDocument doc = new BusinessPlanDocument();
        doc.id = UUID.randomUUID().toString();
        doc.projectId = projectId;
        doc.version = version;
        doc.status = DocumentStatus.GENERATING;
        doc.createdAt = LocalDateTime.now();
        doc.updatedAt = LocalDateTime.now();
        return doc;
    }

    // ==========================================
    // 상태 변경 메서드
    // ==========================================

    /**
     * 생성 완료 처리 - DRAFT 상태로 전환.
     */
    public void markAsGenerated(int generationTimeMs) {
        this.status = DocumentStatus.DRAFT;
        this.generationTimeMs = generationTimeMs;
        this.totalWordCount = calculateTotalWordCount();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 생성 실패 처리.
     */
    public void markAsFailed() {
        this.status = DocumentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 최종 확정 처리.
     */
    public void markAsFinalized() {
        this.status = DocumentStatus.FINALIZED;
        this.updatedAt = LocalDateTime.now();
    }

    // ==========================================
    // 섹션 업데이트 메서드
    // ==========================================

    public void updateExecutiveSummary(String content) {
        this.executiveSummary = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProblemDefinition(String content) {
        this.problemDefinition = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSolution(String content) {
        this.solution = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateMarketAnalysis(String content) {
        this.marketAnalysis = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateBusinessModel(String content) {
        this.businessModel = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCompetitiveAnalysis(String content) {
        this.competitiveAnalysis = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateMarketingStrategy(String content) {
        this.marketingStrategy = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTeam(String content) {
        this.team = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateFinancialPlan(String content) {
        this.financialPlan = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateMilestones(String content) {
        this.milestones = content;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 모든 섹션을 한번에 업데이트.
     */
    public void updateAllSections(
            String executiveSummary,
            String problemDefinition,
            String solution,
            String marketAnalysis,
            String businessModel,
            String competitiveAnalysis,
            String marketingStrategy,
            String team,
            String financialPlan,
            String milestones
    ) {
        this.executiveSummary = executiveSummary;
        this.problemDefinition = problemDefinition;
        this.solution = solution;
        this.marketAnalysis = marketAnalysis;
        this.businessModel = businessModel;
        this.competitiveAnalysis = competitiveAnalysis;
        this.marketingStrategy = marketingStrategy;
        this.team = team;
        this.financialPlan = financialPlan;
        this.milestones = milestones;
        this.updatedAt = LocalDateTime.now();
    }

    // ==========================================
    // 유틸리티 메서드
    // ==========================================

    /**
     * 전체 글자 수 계산.
     */
    private int calculateTotalWordCount() {
        int count = 0;
        if (executiveSummary != null) count += executiveSummary.length();
        if (problemDefinition != null) count += problemDefinition.length();
        if (solution != null) count += solution.length();
        if (marketAnalysis != null) count += marketAnalysis.length();
        if (businessModel != null) count += businessModel.length();
        if (competitiveAnalysis != null) count += competitiveAnalysis.length();
        if (marketingStrategy != null) count += marketingStrategy.length();
        if (team != null) count += team.length();
        if (financialPlan != null) count += financialPlan.length();
        if (milestones != null) count += milestones.length();
        return count;
    }
}

