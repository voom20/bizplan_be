package com.vibe.bizplan.bizplan_be.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import com.vibe.bizplan.bizplan_be.domain.model.ProjectStatus;
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
    private final ObjectMapper objectMapper;

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
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        Objects.requireNonNull(request, "요청 데이터는 필수입니다");
        log.info("Wizard 답변 저장: projectId={}, stepId={}", projectId, request.stepId());
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));
        
        // 기존 답변 파싱
        Map<String, Object> existingAnswers = parseAnswers(project.getWizardAnswers());
        
        // 새 답변 병합 (해당 stepId의 답변만 업데이트)
        existingAnswers.put(request.stepId(), request.answers());
        
        // JSON으로 변환하여 저장
        String updatedJson = toJson(existingAnswers);
        project.updateWizardAnswers(updatedJson);
        
        // 프로젝트 상태 업데이트 (DRAFT → IN_PROGRESS)
        if (project.getStatus() == ProjectStatus.DRAFT) {
            project.changeStatus(ProjectStatus.IN_PROGRESS);
            log.debug("프로젝트 상태 변경: DRAFT → IN_PROGRESS");
        }
        
        projectRepository.save(project);
        log.info("Wizard 답변 저장 완료: projectId={}, completedSteps={}", projectId, existingAnswers.size());
        
        return WizardAnswersResponse.of(projectId, existingAnswers, project.getTemplateCode());
    }

    /**
     * Wizard 전체 답변 조회.
     *
     * @param projectId 프로젝트 ID (null 불가)
     * @return 전체 답변 데이터
     */
    @Transactional(readOnly = true)
    public WizardAnswersResponse getAnswers(String projectId) {
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        log.debug("Wizard 답변 조회: projectId={}", projectId);
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));
        
        Map<String, Object> answers = parseAnswers(project.getWizardAnswers());
        
        return WizardAnswersResponse.of(projectId, answers, project.getTemplateCode());
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
        Objects.requireNonNull(projectId, "프로젝트 ID는 필수입니다");
        Objects.requireNonNull(stepId, "단계 ID는 필수입니다");
        log.debug("특정 단계 답변 조회: projectId={}, stepId={}", projectId, stepId);
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));
        
        Map<String, Object> allAnswers = parseAnswers(project.getWizardAnswers());
        Object stepAnswers = allAnswers.get(stepId);
        
        if (stepAnswers instanceof Map) {
            return (Map<String, Object>) stepAnswers;
        }
        return new HashMap<>();
    }

    /**
     * JSON 문자열을 Map으로 파싱.
     */
    private Map<String, Object> parseAnswers(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 실패: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Map을 JSON 문자열로 변환.
     */
    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 실패: {}", e.getMessage());
            throw new RuntimeException("답변 데이터 변환에 실패했습니다", e);
        }
    }
}

