package com.vibe.bizplan.bizplan_be.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.vibe.bizplan.bizplan_be.domain.model.ProjectStatus;

/**
 * 프로젝트 응답 DTO.
 * 프로젝트 정보를 클라이언트에게 반환할 때 사용한다.
 * 
 * <p>엔티티에서 DTO로 변환 시 {@link ProjectResponseMapper}를 사용하세요.</p>
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
}

