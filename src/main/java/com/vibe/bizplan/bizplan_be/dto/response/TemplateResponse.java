package com.vibe.bizplan.bizplan_be.dto.response;

import com.vibe.bizplan.bizplan_be.domain.model.TemplateCode;

/**
 * 템플릿 응답 DTO.
 * 사용 가능한 템플릿 정보를 클라이언트에게 반환한다.
 */
public record TemplateResponse(
        
        /** 템플릿 코드 */
        String code,
        
        /** 템플릿 표시명 */
        String displayName,
        
        /** 템플릿 설명 */
        String description,
        
        /** 템플릿 카테고리 */
        String category
) {
    
    /**
     * TemplateCode 열거형으로부터 응답 DTO 생성.
     *
     * @param templateCode 템플릿 코드 열거형
     * @return 템플릿 응답 DTO
     */
    public static TemplateResponse from(TemplateCode templateCode) {
        return new TemplateResponse(
                templateCode.name(),
                templateCode.getDisplayName(),
                templateCode.getDescription(),
                templateCode.getCategory()
        );
    }
}

