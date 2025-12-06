package com.vibe.bizplan.bizplan_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 프로젝트 생성 요청 DTO.
 * 클라이언트에서 전송하는 프로젝트 생성 요청 데이터.
 */
public record CreateProjectRequest(
        
        /** 선택된 템플릿 코드 (필수) */
        @NotBlank(message = "템플릿 코드는 필수입니다")
        String templateCode,
        
        /** 프로젝트 제목 (선택, 최대 255자) */
        @Size(max = 255, message = "프로젝트 제목은 255자 이내여야 합니다")
        String title
) {
}

