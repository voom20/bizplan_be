package com.vibe.bizplan.bizplan_be.infrastructure.repository;

import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import com.vibe.bizplan.bizplan_be.domain.model.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 프로젝트 레포지토리 인터페이스.
 * JPA를 통한 프로젝트 엔티티 CRUD 및 조회 기능을 제공한다.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {

    /**
     * 특정 사용자의 프로젝트 목록 조회.
     *
     * @param userId 사용자 ID
     * @return 사용자의 프로젝트 목록
     */
    List<Project> findByUserId(String userId);

    /**
     * 특정 사용자의 특정 상태 프로젝트 조회.
     *
     * @param userId 사용자 ID
     * @param status 프로젝트 상태
     * @return 조건에 맞는 프로젝트 목록
     */
    List<Project> findByUserIdAndStatus(String userId, ProjectStatus status);

    /**
     * 특정 사용자의 프로젝트 개수 조회.
     *
     * @param userId 사용자 ID
     * @return 프로젝트 개수
     */
    long countByUserId(String userId);
}

