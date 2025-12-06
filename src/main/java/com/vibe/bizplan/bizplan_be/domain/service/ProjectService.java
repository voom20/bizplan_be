package com.vibe.bizplan.bizplan_be.domain.service;

import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import com.vibe.bizplan.bizplan_be.domain.model.TemplateCode;
import com.vibe.bizplan.bizplan_be.dto.request.CreateProjectRequest;
import com.vibe.bizplan.bizplan_be.dto.response.ProjectResponse;
import com.vibe.bizplan.bizplan_be.dto.response.ProjectResponseMapper;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 프로젝트 서비스.
 * 사업계획서 프로젝트의 생성, 조회, 수정 등 비즈니스 로직을 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TemplateService templateService;
    private final ProjectResponseMapper projectResponseMapper;

    /** MVP용 기본 사용자 ID (인증 미구현) */
    private static final String DEFAULT_USER_ID = "default-user";

    /**
     * 새 프로젝트 생성.
     *
     * @param request 프로젝트 생성 요청 DTO (null 불가)
     * @return 생성된 프로젝트 응답 DTO
     * @throws IllegalArgumentException 유효하지 않은 템플릿 코드인 경우
     */
    @SuppressWarnings("null") // Spring Data save() 메서드의 null-safety 분석 오탐 억제
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        Objects.requireNonNull(request, "요청 데이터는 필수입니다");
        log.info("프로젝트 생성 요청: templateCode={}", request.templateCode());
        
        // 템플릿 코드 유효성 검증
        TemplateCode templateCode = templateService.validateTemplateCode(request.templateCode());
        
        // 프로젝트 엔티티 생성
        Project project = Project.create(templateCode, DEFAULT_USER_ID);
        
        // 제목이 있으면 설정
        if (request.title() != null && !request.title().isBlank()) {
            project.updateTitle(request.title());
        }
        
        // DB 저장
        Project savedProject = Objects.requireNonNull(
                projectRepository.save(project), "프로젝트 저장 실패");
        log.info("프로젝트 생성 완료: id={}, templateCode={}", savedProject.getId(), savedProject.getTemplateCode());
        
        return projectResponseMapper.toResponse(savedProject);
    }

    /**
     * 프로젝트 ID로 조회.
     *
     * @param projectId 프로젝트 ID (null 불가)
     * @return 프로젝트 응답 DTO (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<ProjectResponse> getProject(String projectId) {
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        log.debug("프로젝트 조회: id={}", projectId);
        return projectRepository.findById(projectId)
                .map(projectResponseMapper::toResponse);
    }

    /**
     * 현재 사용자의 모든 프로젝트 조회.
     * MVP에서는 기본 사용자의 프로젝트만 반환.
     *
     * @return 프로젝트 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getMyProjects() {
        log.debug("사용자 프로젝트 목록 조회: userId={}", DEFAULT_USER_ID);
        return projectResponseMapper.toResponseList(
                projectRepository.findByUserId(DEFAULT_USER_ID)
        );
    }

    /**
     * 프로젝트 엔티티 조회 (내부용).
     *
     * @param projectId 프로젝트 ID (null 불가)
     * @return 프로젝트 엔티티
     * @throws IllegalArgumentException 프로젝트를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Project getProjectEntity(String projectId) {
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));
    }
}

