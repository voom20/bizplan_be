package com.vibe.bizplan.bizplan_be.domain.model;

/**
 * 프로젝트 상태를 나타내는 열거형.
 * 사업계획서의 작성 및 제출 진행 상태를 추적한다.
 */
public enum ProjectStatus {
    
    /** 초안 작성 중 - 기본 상태 */
    DRAFT,
    
    /** 작성 진행 중 - Wizard 단계 진행 */
    IN_PROGRESS,
    
    /** 제출 완료 */
    SUBMITTED,
    
    /** 승인됨 */
    APPROVED,
    
    /** 거절됨 */
    REJECTED
}

