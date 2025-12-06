package com.vibe.bizplan.bizplan_be.dto.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import com.vibe.bizplan.bizplan_be.domain.model.ProjectStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * 프로젝트 응답 DTO.
 * 프로젝트 정보를 클라이언트에게 반환할 때 사용한다.
 */
public record ProjectResponse(
        
        /** 프로젝트 ID */
        String projectId,
        
        /** 템플릿 코드 */
        String templateCode,
        
        /** 프로젝트 제목 */
        String title,
        
        /** 프로젝트 상태 */
        ProjectStatus status,
        
        /** Wizard 답변 데이터 */
        Map<String, Object> wizardAnswers,
        
        /** 생성일시 */
        LocalDateTime createdAt,
        
        /** 수정일시 */
        LocalDateTime updatedAt
) {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Project 엔티티로부터 응답 DTO 생성.
     *
     * @param project 프로젝트 엔티티
     * @return 프로젝트 응답 DTO
     */
    public static ProjectResponse from(Project project) {
        Map<String, Object> answers = parseWizardAnswers(project.getWizardAnswers());
        return new ProjectResponse(
                project.getId(),
                project.getTemplateCode().name(),
                project.getTitle(),
                project.getStatus(),
                answers,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
    
    /**
     * JSON 문자열을 Map으로 파싱.
     */
    private static Map<String, Object> parseWizardAnswers(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }
}

