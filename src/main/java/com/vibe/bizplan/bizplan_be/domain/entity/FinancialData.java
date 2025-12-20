package com.vibe.bizplan.bizplan_be.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 재무 데이터 엔티티.
 * 프로젝트별 재무 가정값과 계산 결과를 저장한다.
 */
@Entity
@Table(name = "financial_data")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FinancialData {

    /** 고유 ID (UUID) */
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    /** 프로젝트 ID */
    @Column(name = "project_id", length = 36, nullable = false, unique = true)
    private String projectId;

    /** 재무 가정값 (JSON) */
    @Column(name = "assumptions", columnDefinition = "TEXT", nullable = false)
    private String assumptions;

    /** 계산된 재무 추정 결과 (JSON) */
    @Column(name = "projection_result", columnDefinition = "TEXT")
    private String projectionResult;

    /** 생성일시 */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정일시 */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 새 재무 데이터 생성을 위한 정적 팩토리 메서드.
     *
     * @param projectId 프로젝트 ID
     * @param assumptions 재무 가정값 JSON
     * @param projectionResult 계산 결과 JSON
     * @return 생성된 FinancialData 인스턴스
     */
    public static FinancialData create(String projectId, String assumptions, String projectionResult) {
        return FinancialData.builder()
                .id(UUID.randomUUID().toString())
                .projectId(projectId)
                .assumptions(assumptions)
                .projectionResult(projectionResult)
                .build();
    }

    /**
     * 재무 데이터 업데이트.
     *
     * @param assumptions 새 가정값 JSON
     * @param projectionResult 새 계산 결과 JSON
     */
    public void update(String assumptions, String projectionResult) {
        this.assumptions = assumptions;
        this.projectionResult = projectionResult;
    }
}

