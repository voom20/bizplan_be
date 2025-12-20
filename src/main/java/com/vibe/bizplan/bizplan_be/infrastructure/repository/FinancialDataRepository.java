package com.vibe.bizplan.bizplan_be.infrastructure.repository;

import com.vibe.bizplan.bizplan_be.domain.entity.FinancialData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 재무 데이터 리포지토리.
 */
@Repository
public interface FinancialDataRepository extends JpaRepository<FinancialData, String> {
    
    /**
     * 프로젝트 ID로 재무 데이터 조회.
     *
     * @param projectId 프로젝트 ID
     * @return 재무 데이터 (Optional)
     */
    Optional<FinancialData> findByProjectId(String projectId);
    
    /**
     * 프로젝트 ID로 재무 데이터 존재 여부 확인.
     *
     * @param projectId 프로젝트 ID
     * @return 존재 여부
     */
    boolean existsByProjectId(String projectId);
}

