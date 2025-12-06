package com.vibe.bizplan.bizplan_be.domain.entity;

import com.vibe.bizplan.bizplan_be.domain.model.ProjectStatus;
import com.vibe.bizplan.bizplan_be.domain.model.TemplateCode;
import com.vibe.bizplan.bizplan_be.infrastructure.security.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사업계획서 프로젝트 엔티티.
 * 사용자가 생성한 사업계획서 프로젝트의 메타데이터를 관리한다.
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Project {

    /** 프로젝트 고유 ID (UUID) */
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    /** 선택된 템플릿 코드 */
    @Enumerated(EnumType.STRING)
    @Column(name = "template_code", length = 50, nullable = false)
    private TemplateCode templateCode;

    /** 프로젝트 제목 (사용자 지정) */
    @Column(name = "title", length = 255)
    private String title;

    /** 프로젝트 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.DRAFT;

    /** 소유자 사용자 ID (MVP에서는 Mocking 가능) */
    @Column(name = "user_id", length = 36)
    private String userId;

    /** Wizard 단계별 답변 데이터 (JSON 문자열, AES-256 암호화) */
    @Column(name = "wizard_answers", columnDefinition = "TEXT")
    @Convert(converter = EncryptedStringConverter.class)
    private String wizardAnswers;

    /** 생성일시 */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정일시 */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 새 프로젝트 생성을 위한 정적 팩토리 메서드.
     * UUID를 자동 생성하고 기본 상태를 DRAFT로 설정한다.
     *
     * @param templateCode 선택된 템플릿 코드
     * @param userId 소유자 사용자 ID
     * @return 생성된 프로젝트 인스턴스
     */
    public static Project create(TemplateCode templateCode, String userId) {
        return Project.builder()
                .id(UUID.randomUUID().toString())
                .templateCode(templateCode)
                .userId(userId)
                .status(ProjectStatus.DRAFT)
                .build();
    }

    /**
     * 프로젝트 제목 설정.
     *
     * @param title 새 제목
     */
    public void updateTitle(String title) {
        this.title = title;
    }

    /**
     * 프로젝트 상태 변경.
     *
     * @param status 새 상태
     */
    public void changeStatus(ProjectStatus status) {
        this.status = status;
    }

    /**
     * Wizard 답변 업데이트.
     *
     * @param wizardAnswers JSON 형태의 답변 문자열
     */
    public void updateWizardAnswers(String wizardAnswers) {
        this.wizardAnswers = wizardAnswers;
    }
}

