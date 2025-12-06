package com.vibe.bizplan.bizplan_be.dto.response;

import com.vibe.bizplan.bizplan_be.domain.entity.Project;
import com.vibe.bizplan.bizplan_be.domain.model.ProjectStatus;

import java.time.LocalDateTime;

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
        
        /** 생성일시 */
        LocalDateTime createdAt,
        
        /** 수정일시 */
        LocalDateTime updatedAt
) {
    
    /**
     * Project 엔티티로부터 응답 DTO 생성.
     *
     * @param project 프로젝트 엔티티
     * @return 프로젝트 응답 DTO
     */
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getTemplateCode().name(),
                project.getTitle(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}

