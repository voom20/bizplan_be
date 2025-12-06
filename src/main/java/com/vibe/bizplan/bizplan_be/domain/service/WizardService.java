package com.vibe.bizplan.bizplan_be.domain.service;

import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import com.vibe.bizplan.bizplan_be.domain.exception.ProjectNotFoundException;
import com.vibe.bizplan.bizplan_be.domain.model.ProjectStatus;
import com.vibe.bizplan.bizplan_be.domain.service.util.JsonParsingUtil;
import com.vibe.bizplan.bizplan_be.dto.request.SaveWizardAnswersRequest;
import com.vibe.bizplan.bizplan_be.dto.response.WizardAnswersResponse;
import com.vibe.bizplan.bizplan_be.infrastructure.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Wizard 서비스.
 * Wizard 단계별 답변 저장 및 조회 로직을 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WizardService {

    private final ProjectRepository projectRepository;
    private final JsonParsingUtil jsonParsingUtil;

    /**
     * Wizard 답변 저장 (Upsert).
     * 기존 답변에 새로운 답변을 병합하여 저장한다.
     *
     * @param projectId 프로젝트 ID (null 불가)
     * @param request 답변 저장 요청 (null 불가)
     * @return 업데이트된 전체 답변
     */
    @Transactional
    public WizardAnswersResponse saveAnswers(String projectId, SaveWizardAnswersRequest request) {
        // [Stage 1] 요청 수신 및 파라미터 검증
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        Objects.requireNonNull(request, "요청 데이터는 필수입니다");
        String stepId = request.stepId();
        int answersFieldCount = request.answers() != null ? request.answers().size() : 0;
        
        log.info("[Wizard] 답변 저장 시작 - projectId={}, stepId={}, fieldsCount={}", 
                projectId, stepId, answersFieldCount);
        
        try {
            // [Stage 2] 프로젝트 조회 및 검증
            log.debug("[Wizard] 프로젝트 조회 중 - projectId={}", projectId);
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> {
                        log.warn("[Wizard] 프로젝트 조회 실패 - projectId={} (존재하지 않음)", projectId);
                        return new ProjectNotFoundException(projectId);
                    });
            
            String templateCode = project.getTemplateCode().name();
            log.debug("[Wizard] 프로젝트 조회 완료 - projectId={}, templateCode={}, currentStatus={}", 
                    projectId, templateCode, project.getStatus());
            
            // [Stage 3] 기존 답변 파싱 및 병합
            log.debug("[Wizard] 기존 답변 파싱 시작 - projectId={}, stepId={}", projectId, stepId);
            Map<String, Object> existingAnswers = jsonParsingUtil.parseToMap(project.getWizardAnswers());
            int previousStepCount = existingAnswers.size();
            boolean isNewStep = !existingAnswers.containsKey(stepId);
            
            existingAnswers.put(stepId, request.answers());
            log.debug("[Wizard] 답변 병합 완료 - projectId={}, stepId={}, isNewStep={}, previousSteps={}, currentSteps={}", 
                    projectId, stepId, isNewStep, previousStepCount, existingAnswers.size());
            
            // [Stage 4] 데이터 저장 (JSON 변환 및 영속화)
            log.debug("[Wizard] 답변 저장 시작 - projectId={}, stepId={}", projectId, stepId);
            String updatedJson = jsonParsingUtil.toJson(existingAnswers);
            project.updateWizardAnswers(updatedJson);
            
            // 프로젝트 상태 업데이트 (DRAFT → IN_PROGRESS)
            ProjectStatus previousStatus = project.getStatus();
            if (previousStatus == ProjectStatus.DRAFT) {
                project.changeStatus(ProjectStatus.IN_PROGRESS);
                log.info("[Wizard] 프로젝트 상태 변경 - projectId={}, {} → {}", 
                        projectId, previousStatus, ProjectStatus.IN_PROGRESS);
            }
            
            projectRepository.save(project);
            log.debug("[Wizard] 답변 저장 완료 - projectId={}, stepId={}", projectId, stepId);
            
            // [Stage 5] 응답 생성 및 완료 로깅
            WizardAnswersResponse response = WizardAnswersResponse.of(projectId, existingAnswers, project.getTemplateCode());
            log.info("[Wizard] 답변 저장 성공 - projectId={}, stepId={}, completedSteps={}/{}, templateCode={}", 
                    projectId, stepId, response.completedSteps(), response.totalSteps(), templateCode);
            
            return response;
            
        } catch (IllegalArgumentException e) {
            // 비즈니스 로직 예외 (프로젝트 미존재 등)
            log.error("[Wizard] 답변 저장 실패 (비즈니스 오류) - projectId={}, stepId={}, error={}", 
                    projectId, stepId, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            // 시스템 예외 (JSON 변환 실패 등)
            log.error("[Wizard] 답변 저장 실패 (시스템 오류) - projectId={}, stepId={}", 
                    projectId, stepId, e);
            throw e;
        }
    }

    /**
     * Wizard 전체 답변 조회.
     *
     * @param projectId 프로젝트 ID (null 불가)
     * @return 전체 답변 데이터
     */
    @Transactional(readOnly = true)
    public WizardAnswersResponse getAnswers(String projectId) {
        // [Stage 1] 파라미터 검증
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        log.info("[Wizard] 전체 답변 조회 시작 - projectId={}", projectId);
        
        try {
            // [Stage 2] 프로젝트 조회
            log.debug("[Wizard] 프로젝트 조회 중 - projectId={}", projectId);
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> {
                        log.warn("[Wizard] 프로젝트 조회 실패 - projectId={} (존재하지 않음)", projectId);
                        return new ProjectNotFoundException(projectId);
                    });
            
            String templateCode = project.getTemplateCode().name();
            log.debug("[Wizard] 프로젝트 조회 완료 - projectId={}, templateCode={}", projectId, templateCode);
            
            // [Stage 3] 답변 데이터 파싱
            Map<String, Object> answers = jsonParsingUtil.parseToMap(project.getWizardAnswers());
            
            // [Stage 4] 응답 생성 및 완료 로깅
            WizardAnswersResponse response = WizardAnswersResponse.of(projectId, answers, project.getTemplateCode());
            log.info("[Wizard] 전체 답변 조회 완료 - projectId={}, completedSteps={}/{}, templateCode={}", 
                    projectId, response.completedSteps(), response.totalSteps(), templateCode);
            
            return response;
            
        } catch (ProjectNotFoundException e) {
            log.error("[Wizard] 전체 답변 조회 실패 - projectId={}, error={}", projectId, e.getMessage());
            throw e;
        }
    }

    /**
     * 특정 단계의 답변만 조회.
     *
     * @param projectId 프로젝트 ID (null 불가)
     * @param stepId 단계 ID (null 불가)
     * @return 해당 단계의 답변 (없으면 빈 Map)
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<String, Object> getStepAnswers(String projectId, String stepId) {
        // [Stage 1] 파라미터 검증
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        Objects.requireNonNull(stepId, "단계 ID는 필수입니다");
        log.info("[Wizard] 단계별 답변 조회 시작 - projectId={}, stepId={}", projectId, stepId);
        
        try {
            // [Stage 2] 프로젝트 조회
            log.debug("[Wizard] 프로젝트 조회 중 - projectId={}, stepId={}", projectId, stepId);
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> {
                        log.warn("[Wizard] 프로젝트 조회 실패 - projectId={}, stepId={} (프로젝트 미존재)", 
                                projectId, stepId);
                        return new ProjectNotFoundException(projectId);
                    });
            
            // [Stage 3] 답변 데이터 파싱 및 추출
            Map<String, Object> allAnswers = jsonParsingUtil.parseToMap(project.getWizardAnswers());
            Object stepAnswers = allAnswers.get(stepId);
            
            // [Stage 4] 결과 반환
            if (stepAnswers instanceof Map) {
                Map<String, Object> result = (Map<String, Object>) stepAnswers;
                log.info("[Wizard] 단계별 답변 조회 완료 - projectId={}, stepId={}, fieldsCount={}", 
                        projectId, stepId, result.size());
                return result;
            }
            
            log.info("[Wizard] 단계별 답변 조회 완료 - projectId={}, stepId={}, fieldsCount=0 (답변 없음)", 
                    projectId, stepId);
            return new HashMap<>();
            
        } catch (ProjectNotFoundException e) {
            log.error("[Wizard] 단계별 답변 조회 실패 - projectId={}, stepId={}, error={}", 
                    projectId, stepId, e.getMessage());
            throw e;
        }
    }
}

