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
        // [Stage 1] 요청 검증
        Objects.requireNonNull(request, "요청 데이터는 필수입니다");
        String requestedTemplateCode = request.templateCode();
        String requestedTitle = request.title();
        
        log.info("[Project] 프로젝트 생성 시작 - templateCode={}, title={}", 
                requestedTemplateCode, requestedTitle);
        
        try {
            // [Stage 2] 템플릿 코드 유효성 검증
            log.debug("[Project] 템플릿 코드 검증 중 - templateCode={}", requestedTemplateCode);
            TemplateCode templateCode = templateService.validateTemplateCode(requestedTemplateCode);
            log.debug("[Project] 템플릿 코드 검증 완료 - templateCode={}, totalSteps={}", 
                    templateCode.name(), templateCode.getTotalSteps());
            
            // [Stage 3] 프로젝트 엔티티 생성
            log.debug("[Project] 프로젝트 엔티티 생성 중 - userId={}", DEFAULT_USER_ID);
            Project project = Project.create(templateCode, DEFAULT_USER_ID);
            
            if (requestedTitle != null && !requestedTitle.isBlank()) {
                project.updateTitle(requestedTitle);
                log.debug("[Project] 프로젝트 제목 설정 - title={}", requestedTitle);
            }
            
            // [Stage 4] DB 저장
            log.debug("[Project] 프로젝트 저장 중 - projectId={}", project.getId());
            Project savedProject = Objects.requireNonNull(
                    projectRepository.save(project), "프로젝트 저장 실패");
            
            // [Stage 5] 응답 생성 및 완료 로깅
            ProjectResponse response = projectResponseMapper.toResponse(savedProject);
            log.info("[Project] 프로젝트 생성 완료 - projectId={}, templateCode={}, status={}", 
                    savedProject.getId(), savedProject.getTemplateCode(), savedProject.getStatus());
            
            return response;
            
        } catch (IllegalArgumentException e) {
            log.error("[Project] 프로젝트 생성 실패 (유효성 오류) - templateCode={}, error={}", 
                    requestedTemplateCode, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[Project] 프로젝트 생성 실패 (시스템 오류) - templateCode={}", 
                    requestedTemplateCode, e);
            throw e;
        }
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
     * 현재 사용자의 모든 프로젝트 조회.
     * MVP에서는 기본 사용자의 프로젝트만 반환.
     *
     * @return 프로젝트 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getMyProjects() {
        log.info("[Project] 프로젝트 목록 조회 시작 - userId={}", DEFAULT_USER_ID);
        
        List<Project> projects = projectRepository.findByUserId(DEFAULT_USER_ID);
        List<ProjectResponse> response = projectResponseMapper.toResponseList(projects);
        
        log.info("[Project] 프로젝트 목록 조회 완료 - userId={}, count={}", DEFAULT_USER_ID, response.size());
        return response;
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
        log.debug("[Project] 프로젝트 엔티티 조회 - projectId={}", projectId);
        
        return projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("[Project] 프로젝트 엔티티 조회 실패 - projectId={} (존재하지 않음)", projectId);
                    return new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId);
                });
    }
}

