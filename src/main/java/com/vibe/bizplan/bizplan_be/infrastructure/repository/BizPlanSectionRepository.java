package com.vibe.bizplan.bizplan_be.infrastructure.repository;

import com.vibe.bizplan.bizplan_be.domain.entity.BizPlanSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사업계획서 섹션 리포지토리.
 */
@Repository
public interface BizPlanSectionRepository extends JpaRepository<BizPlanSection, String> {

    /**
     * 프로젝트의 모든 섹션 조회.
     *
     * @param projectId 프로젝트 ID
     * @return 섹션 목록
     */
    List<BizPlanSection> findByProjectIdOrderBySectionType(String projectId);

    /**
     * 프로젝트의 특정 섹션 조회.
     *
     * @param projectId   프로젝트 ID
     * @param sectionType 섹션 타입
     * @return 섹션 (Optional)
     */
    Optional<BizPlanSection> findByProjectIdAndSectionType(String projectId, String sectionType);

    /**
     * 프로젝트의 섹션 개수 조회.
     *
     * @param projectId 프로젝트 ID
     * @return 섹션 개수
     */
    int countByProjectId(String projectId);

    /**
     * 프로젝트의 모든 섹션 삭제.
     *
     * @param projectId 프로젝트 ID
     */
    void deleteByProjectId(String projectId);

    /**
     * 프로젝트의 특정 섹션 삭제.
     *
     * @param projectId   프로젝트 ID
     * @param sectionType 섹션 타입
     */
    void deleteByProjectIdAndSectionType(String projectId, String sectionType);
}

