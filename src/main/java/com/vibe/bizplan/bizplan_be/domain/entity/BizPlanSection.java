package com.vibe.bizplan.bizplan_be.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사업계획서 섹션 엔티티.
 * AI 생성 섹션 내용을 저장한다.
 */
@Entity
@Table(name = "bizplan_sections",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_project_section",
                columnNames = {"project_id", "section_type"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BizPlanSection {

    /** 섹션 ID */
    @Id
    @Column(length = 36)
    private String id;

    /** 프로젝트 ID */
    @Column(name = "project_id", nullable = false, length = 36)
    private String projectId;

    /** 섹션 타입 (executive_summary, problem_definition 등) */
    @Column(name = "section_type", nullable = false, length = 50)
    private String sectionType;

    /** 섹션 제목 */
    @Column(nullable = false, length = 255)
    private String title;

    /** 섹션 내용 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 글자 수 */
    @Column(name = "word_count")
    private Integer wordCount;

    /** 사용된 AI 모델 */
    @Column(name = "model_used", length = 100)
    private String modelUsed;

    /** 생성 소요 시간 (ms) */
    @Column(name = "generation_time_ms")
    private Integer generationTimeMs;

    /** 생성일시 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 수정일시 */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 섹션 생성 팩토리 메서드.
     */
    public static BizPlanSection create(
            String projectId,
            String sectionType,
            String title,
            String content,
            Integer wordCount,
            String modelUsed,
            Integer generationTimeMs
    ) {
        BizPlanSection section = new BizPlanSection();
        section.id = UUID.randomUUID().toString();
        section.projectId = projectId;
        section.sectionType = sectionType;
        section.title = title;
        section.content = content;
        section.wordCount = wordCount;
        section.modelUsed = modelUsed;
        section.generationTimeMs = generationTimeMs;
        return section;
    }

    /**
     * 섹션 내용 업데이트.
     */
    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
        this.wordCount = content != null ? content.length() : 0;
    }

    /**
     * AI 생성 결과로 업데이트.
     */
    public void updateFromAi(
            String title,
            String content,
            Integer wordCount,
            String modelUsed,
            Integer generationTimeMs
    ) {
        this.title = title;
        this.content = content;
        this.wordCount = wordCount;
        this.modelUsed = modelUsed;
        this.generationTimeMs = generationTimeMs;
    }
}

