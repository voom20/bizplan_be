package com.vibe.bizplan.bizplan_be.dto.request;

import com.vibe.bizplan.bizplan_be.domain.model.ProjectStatus;
import jakarta.validation.constraints.Size;

/**
 * 프로젝트 수정 요청 DTO.
 * 클라이언트에서 전송하는 프로젝트 수정 요청 데이터.
 * 모든 필드는 선택적(Optional)이며, 제공된 필드만 업데이트된다.
 */
public record UpdateProjectRequest(
        
        /** 프로젝트 제목 (선택, 최대 255자) */
        @Size(max = 255, message = "프로젝트 제목은 255자 이내여야 합니다")
        String title,
        
        /** 프로젝트 상태 (선택) */
        ProjectStatus status
) {
}

