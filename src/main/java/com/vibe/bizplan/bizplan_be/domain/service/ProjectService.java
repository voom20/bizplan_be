package com.vibe.bizplan.bizplan_be.domain.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import com.vibe.bizplan.bizplan_be.domain.exception.ProjectLimitExceededException;
import com.vibe.bizplan.bizplan_be.domain.exception.ProjectNotFoundException;
import com.vibe.bizplan.bizplan_be.domain.exception.ResourceAccessDeniedException;
import com.vibe.bizplan.bizplan_be.domain.model.BizPlanConstants;
import com.vibe.bizplan.bizplan_be.domain.model.TemplateCode;
import com.vibe.bizplan.bizplan_be.domain.model.UserRole;
import com.vibe.bizplan.bizplan_be.dto.request.CreateProjectRequest;
import com.vibe.bizplan.bizplan_be.dto.response.ProjectResponse;
import com.vibe.bizplan.bizplan_be.dto.response.ProjectResponseMapper;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    /** 무료 사용자 최대 프로젝트 개수 */
    private static final int MAX_PROJECTS_FREE_USER = 5;

    /**
     * 새 프로젝트 생성.
     *
     * @param request 프로젝트 생성 요청 DTO (null 불가)
     * @param userId 생성자 사용자 ID
     * @param userRole 사용자 역할
     * @return 생성된 프로젝트 응답 DTO
     * @throws IllegalArgumentException 유효하지 않은 템플릿 코드인 경우
     * @throws ProjectLimitExceededException 무료 사용자가 프로젝트 한도 초과 시
     */
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, String userId, UserRole userRole) {
        Objects.requireNonNull(request, "요청 데이터는 필수입니다");
        String requestedTemplateCode = request.templateCode();
        String requestedTitle = request.title();
        
        log.info("[Project] 프로젝트 생성 시작 - userId={}, templateCode={}, title={}", 
                userId, requestedTemplateCode, requestedTitle);
        
        // 무료 사용자 프로젝트 개수 제한 체크
        if (userRole == UserRole.USER) {
            long currentCount = projectRepository.countByUserId(userId);
            if (currentCount >= MAX_PROJECTS_FREE_USER) {
                log.warn("[Project] 프로젝트 생성 한도 초과 - userId={}, count={}", userId, currentCount);
                throw new ProjectLimitExceededException((int) currentCount, MAX_PROJECTS_FREE_USER);
            }
        }
        
        try {
            TemplateCode templateCode = templateService.validateTemplateCode(requestedTemplateCode);
            Project project = Project.create(templateCode, userId);
            
            if (requestedTitle != null && !requestedTitle.isBlank()) {
                project.updateTitle(requestedTitle);
            }
            
            Project savedProject = projectRepository.save(project);
            ProjectResponse response = projectResponseMapper.toResponse(savedProject);
            
            log.info("[Project] 프로젝트 생성 완료 - projectId={}, userId={}", savedProject.getId(), userId);
            return response;
            
        } catch (IllegalArgumentException e) {
            log.error("[Project] 프로젝트 생성 실패 (유효성 오류) - templateCode={}, error={}", 
                    requestedTemplateCode, e.getMessage());
            throw e;
        }
    }

    /**
     * 새 프로젝트 생성 (MVP 호환용 - 기본 사용자).
     *
     * @param request 프로젝트 생성 요청 DTO
     * @return 생성된 프로젝트 응답 DTO
     */
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        return createProject(request, BizPlanConstants.DEFAULT_USER_ID, UserRole.USER);
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
        log.info("[Project] 프로젝트 조회 시작 - projectId={}", projectId);
        
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            log.info("[Project] 프로젝트 조회 완료 - projectId={}, templateCode={}, status={}", 
                    projectId, project.getTemplateCode(), project.getStatus());
            return Optional.of(projectResponseMapper.toResponse(project));
        } else {
            log.warn("[Project] 프로젝트 조회 실패 - projectId={} (존재하지 않음)", projectId);
            return Optional.empty();
        }
    }

    /**
     * 프로젝트 조회 (소유권 검증 포함).
     *
     * @param projectId 프로젝트 ID
     * @param userId 요청자 사용자 ID
     * @param userRole 요청자 역할
     * @return 프로젝트 응답 DTO (Optional)
     * @throws ResourceAccessDeniedException 접근 권한이 없는 경우
     */
    @Transactional(readOnly = true)
    public Optional<ProjectResponse> getProject(String projectId, String userId, UserRole userRole) {
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        log.info("[Project] 프로젝트 조회 시작 - projectId={}, userId={}", projectId, userId);
        
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            
            // 소유권 검증 (ADMIN은 모든 프로젝트 접근 가능)
            if (userRole != UserRole.ADMIN && !project.getUserId().equals(userId)) {
                log.warn("[Project] 프로젝트 접근 거부 - projectId={}, userId={}, ownerId={}", 
                        projectId, userId, project.getUserId());
                throw new ResourceAccessDeniedException("Project", projectId);
            }
            
            log.info("[Project] 프로젝트 조회 완료 - projectId={}", projectId);
            return Optional.of(projectResponseMapper.toResponse(project));
        } else {
            log.warn("[Project] 프로젝트 조회 실패 - projectId={} (존재하지 않음)", projectId);
            return Optional.empty();
        }
    }

    /**
     * 특정 사용자의 모든 프로젝트 조회.
     *
     * @param userId 사용자 ID
     * @return 프로젝트 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByUserId(String userId) {
        log.info("[Project] 프로젝트 목록 조회 시작 - userId={}", userId);
        
        List<Project> projects = projectRepository.findByUserId(userId);
        List<ProjectResponse> response = projectResponseMapper.toResponseList(projects);
        
        log.info("[Project] 프로젝트 목록 조회 완료 - userId={}, count={}", userId, response.size());
        return response;
    }

    /**
     * 현재 사용자의 모든 프로젝트 조회 (MVP 호환용).
     *
     * @return 프로젝트 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getMyProjects() {
        return getProjectsByUserId(BizPlanConstants.DEFAULT_USER_ID);
    }

    /**
     * 프로젝트 엔티티 조회 (내부용).
     *
     * @param projectId 프로젝트 ID (null 불가)
     * @return 프로젝트 엔티티
     * @throws ProjectNotFoundException 프로젝트를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public Project getProjectEntity(String projectId) {
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        log.debug("[Project] 프로젝트 엔티티 조회 - projectId={}", projectId);
        
        return projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("[Project] 프로젝트 엔티티 조회 실패 - projectId={} (존재하지 않음)", projectId);
                    return new ProjectNotFoundException(projectId);
                });
    }

    /**
     * 프로젝트 엔티티 조회 (소유권 검증 포함, 내부용).
     *
     * @param projectId 프로젝트 ID
     * @param userId 요청자 사용자 ID
     * @param userRole 요청자 역할
     * @return 프로젝트 엔티티
     * @throws ProjectNotFoundException 프로젝트를 찾을 수 없는 경우
     * @throws ResourceAccessDeniedException 접근 권한이 없는 경우
     */
    @Transactional(readOnly = true)
    public Project getProjectEntity(String projectId, String userId, UserRole userRole) {
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        
        Project project = getProjectEntity(projectId);
        
        // 소유권 검증 (ADMIN은 모든 프로젝트 접근 가능)
        if (userRole != UserRole.ADMIN && !project.getUserId().equals(userId)) {
            log.warn("[Project] 프로젝트 접근 거부 - projectId={}, userId={}, ownerId={}", 
                    projectId, userId, project.getUserId());
            throw new ResourceAccessDeniedException("Project", projectId);
        }
        
        return project;
    }
}
