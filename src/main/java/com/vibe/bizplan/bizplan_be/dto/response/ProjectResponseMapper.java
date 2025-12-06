package com.vibe.bizplan.bizplan_be.dto.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Project 엔티티를 ProjectResponse DTO로 변환하는 매퍼 컴포넌트.
 * Spring DI를 통해 ObjectMapper를 주입받아 JSON 파싱을 수행한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectResponseMapper {

    private final ObjectMapper objectMapper;

    /**
     * Project 엔티티를 ProjectResponse DTO로 변환.
     *
     * @param project 프로젝트 엔티티
     * @return 프로젝트 응답 DTO
     */
    public ProjectResponse toResponse(Project project) {
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
     * Project 엔티티 목록을 ProjectResponse DTO 목록으로 변환.
     *
     * @param projects 프로젝트 엔티티 목록
     * @return 프로젝트 응답 DTO 목록
     */
    public List<ProjectResponse> toResponseList(List<Project> projects) {
        return projects.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * JSON 문자열을 Map으로 파싱.
     * Spring이 관리하는 ObjectMapper를 사용하여 thread-safe하게 파싱.
     *
     * @param json JSON 문자열
     * @return 파싱된 Map, 실패 시 빈 Map 반환
     */
    private Map<String, Object> parseWizardAnswers(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Wizard 답변 JSON 파싱 실패: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}

