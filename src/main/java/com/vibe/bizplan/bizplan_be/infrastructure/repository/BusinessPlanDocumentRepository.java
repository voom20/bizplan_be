package com.vibe.bizplan.bizplan_be.infrastructure.repository;

import com.vibe.bizplan.bizplan_be.domain.entity.BusinessPlanDocument;
import com.vibe.bizplan.bizplan_be.domain.model.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사업계획서 문서 레포지토리.
 */
@Repository
public interface BusinessPlanDocumentRepository extends JpaRepository<BusinessPlanDocument, String> {

    /**
     * 프로젝트의 모든 문서 조회 (버전 내림차순).
     */
    List<BusinessPlanDocument> findByProjectIdOrderByVersionDesc(String projectId);

    /**
     * 프로젝트의 최신 문서 조회.
     */
    Optional<BusinessPlanDocument> findFirstByProjectIdOrderByVersionDesc(String projectId);

    /**
     * 프로젝트의 특정 버전 문서 조회.
     */
    Optional<BusinessPlanDocument> findByProjectIdAndVersion(String projectId, int version);

    /**
     * 프로젝트의 특정 상태 문서 조회.
     */
    List<BusinessPlanDocument> findByProjectIdAndStatus(String projectId, DocumentStatus status);

    /**
     * 프로젝트의 최대 버전 번호 조회.
     */
    @Query("SELECT COALESCE(MAX(d.version), 0) FROM BusinessPlanDocument d WHERE d.projectId = :projectId")
    int findMaxVersionByProjectId(String projectId);

    /**
     * 프로젝트의 문서 개수 조회.
     */
    long countByProjectId(String projectId);
}

